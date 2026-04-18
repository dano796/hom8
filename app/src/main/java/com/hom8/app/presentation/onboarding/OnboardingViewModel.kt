package com.hom8.app.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.hom8.app.data.local.dao.HomeDao
import com.hom8.app.data.local.dao.UserDao
import com.hom8.app.data.local.entity.HomeEntity
import com.hom8.app.data.local.entity.UserEntity
import com.hom8.app.data.remote.FirestoreRepository
import com.hom8.app.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

data class OnboardingUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val isSetupComplete: Boolean = false,
    val error: String? = null,
    val currentStep: Int = 0
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userDao: UserDao,
    private val homeDao: HomeDao,
    private val session: SessionManager,
    private val firestoreRepo: FirestoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun goToStep(step: Int) {
        _uiState.update { it.copy(currentStep = step) }
    }

    private suspend fun nextStepAfterAuth() {
        // 1. Check SharedPreferences (fast path)
        var hogarId = session.hogarId
        // 2. If cleared by logout, scan the local DB for any home this user belongs to
        if (hogarId.isEmpty()) {
            val userId = session.userId
            if (userId.isNotEmpty()) {
                val home = homeDao.findHomeByMember("%$userId%")
                if (home != null) {
                    session.hogarId = home.id
                    hogarId = home.id
                }
            }
        }
        // 3. If still empty, try to fetch from Firestore
        if (hogarId.isEmpty()) {
            val userId = session.userId
            if (userId.isNotEmpty()) {
                hogarId = fetchUserHomeFromFirestore(userId)
                if (hogarId.isNotEmpty()) {
                    session.hogarId = hogarId
                }
            }
        }
        // 4. Check if there's a pending invite code from a deep link
        if (hogarId.isEmpty() && session.pendingInviteCode.isNotEmpty()) {
            val inviteCode = session.pendingInviteCode
            session.pendingInviteCode = "" // Clear it immediately
            // Process the invite code
            joinHome(inviteCode)
            return // joinHome will handle the next step
        }
        if (hogarId.isNotEmpty()) {
            _uiState.update { it.copy(isLoading = false, isSetupComplete = true) }
        } else {
            _uiState.update { it.copy(isLoading = false, isAuthenticated = true, currentStep = 3) }
        }
    }

    /**
     * Busca en Firestore los hogares donde el usuario es miembro
     * y sincroniza el primero encontrado a la base de datos local
     */
    private suspend fun fetchUserHomeFromFirestore(userId: String): String {
        return try {
            val querySnapshot = auth.currentUser?.let {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("homes")
                    .whereArrayContains("memberIds", userId)
                    .get()
                    .await()
            }
            
            if (querySnapshot != null && !querySnapshot.isEmpty) {
                val homeDoc = querySnapshot.documents.first()
                val memberIds = homeDoc.get("memberIds") as? List<String> ?: emptyList()
                val miembros = "[" + memberIds.joinToString(",") { "\"$it\"" } + "]"
                
                val home = HomeEntity(
                    id = homeDoc.getString("id") ?: homeDoc.id,
                    nombre = homeDoc.getString("nombre") ?: "Mi Hogar",
                    codigoInvitacion = homeDoc.getString("codigoInvitacion") ?: "",
                    miembros = miembros,
                    gastosModo = homeDoc.getString("gastosModo") ?: "SPLIT",
                    creadoEn = homeDoc.getLong("creadoEn") ?: System.currentTimeMillis(),
                    synced = 1
                )
                
                // Guardar en la base de datos local
                homeDao.insertHome(home)
                firestoreRepo.syncHome(home)
                
                home.id
            } else {
                ""
            }
        } catch (e: Exception) {
            // Si falla (sin conexión, Firebase no configurado, etc.), retornar vacío
            ""
        }
    }

    fun signInWithEmail(email: String, password: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // Try Firebase first
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user ?: throw Exception("Error al iniciar sesión")
                saveUserSession(user.uid, user.displayName ?: email.substringBefore("@"), email)
                nextStepAfterAuth()
            } catch (e: Exception) {
                if (isFirebaseConfigError(e)) {
                    // Firebase not configured — fall back to local auth
                    signInLocal(email, password)
                } else {
                    val msg = friendlyError(e)
                    _uiState.update { it.copy(isLoading = false, error = msg) }
                }
            }
        }
    }

    fun signUpWithEmail(email: String, password: String, name: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user ?: throw Exception("Error al registrarse")
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(name).build()
                user.updateProfile(profileUpdates).await()
                saveUserSession(user.uid, name, email)
                nextStepAfterAuth()
            } catch (e: Exception) {
                if (isFirebaseConfigError(e)) {
                    signUpLocal(email, password, name)
                } else {
                    val msg = friendlyError(e)
                    _uiState.update { it.copy(isLoading = false, error = msg) }
                }
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val user = result.user ?: throw Exception("Error al iniciar sesión con Google")
                saveUserSession(user.uid, user.displayName ?: "Usuario", user.email ?: "")
                nextStepAfterAuth()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = friendlyError(e)) }
            }
        }
    }

    fun createHome(homeName: String) {
        val userId = session.userId
        if (userId.isEmpty()) {
            _uiState.update { it.copy(error = "No autenticado") }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val homeId = UUID.randomUUID().toString()
                val code = generateInviteCode()
                val home = HomeEntity(
                    id = homeId,
                    nombre = homeName,
                    codigoInvitacion = code,
                    miembros = "[\"$userId\"]",
                    creadoEn = System.currentTimeMillis()
                )
                homeDao.insertHome(home)
                firestoreRepo.syncHome(home)
                session.hogarId = homeId
                _uiState.update { it.copy(isLoading = false, isSetupComplete = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al crear el hogar") }
            }
        }
    }

    fun joinHome(inviteCode: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // 1. Try to find the home in local database first
                var home = homeDao.getHomeByInviteCode(inviteCode)
                
                // 2. If not found locally, search in Firestore
                if (home == null) {
                    home = fetchHomeFromFirestoreByCode(inviteCode)
                }
                
                if (home != null) {
                    val userId = session.userId
                    // Add the joining user to the home's members list so that
                    // nextStepAfterAuth() can find this home after a re-login.
                    if (userId.isNotEmpty()) {
                        val currentMembers = parseMembersJson(home.miembros)
                        if (!currentMembers.contains(userId)) {
                            val updatedHome = home.copy(
                                miembros = buildMembersJson(currentMembers + userId)
                            )
                            homeDao.updateHome(updatedHome)
                            firestoreRepo.syncHome(updatedHome)
                        }
                    }
                    session.hogarId = home.id
                    _uiState.update { it.copy(isLoading = false, isSetupComplete = true) }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Código de invitación inválido. Asegúrate de ingresarlo tal como fue compartido.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al unirse al hogar") }
            }
        }
    }

    /**
     * Busca un hogar en Firestore por código de invitación
     * y lo sincroniza a la base de datos local
     */
    private suspend fun fetchHomeFromFirestoreByCode(inviteCode: String): HomeEntity? {
        return try {
            val querySnapshot = FirebaseFirestore.getInstance()
                .collection("homes")
                .whereEqualTo("codigoInvitacion", inviteCode.trim().uppercase())
                .limit(1)
                .get()
                .await()
            
            if (!querySnapshot.isEmpty) {
                val homeDoc = querySnapshot.documents.first()
                val memberIds = homeDoc.get("memberIds") as? List<String> ?: emptyList()
                val miembros = "[" + memberIds.joinToString(",") { "\"$it\"" } + "]"
                
                val home = HomeEntity(
                    id = homeDoc.getString("id") ?: homeDoc.id,
                    nombre = homeDoc.getString("nombre") ?: "Mi Hogar",
                    codigoInvitacion = homeDoc.getString("codigoInvitacion") ?: "",
                    miembros = miembros,
                    gastosModo = homeDoc.getString("gastosModo") ?: "SPLIT",
                    creadoEn = homeDoc.getLong("creadoEn") ?: System.currentTimeMillis(),
                    synced = 1
                )
                
                // Guardar en la base de datos local
                homeDao.insertHome(home)
                
                home
            } else {
                null
            }
        } catch (e: Exception) {
            // Si falla (sin conexión, Firebase no configurado, etc.), retornar null
            null
        }
    }

    // ─── JSON helpers ─────────────────────────────────────────────────────────

    private fun parseMembersJson(json: String): List<String> {
        val trimmed = json.trim().removePrefix("[").removeSuffix("]")
        if (trimmed.isBlank()) return emptyList()
        return trimmed.split(",").map { it.trim().removeSurrounding("\"") }.filter { it.isNotEmpty() }
    }

    private fun buildMembersJson(members: List<String>): String =
        "[" + members.joinToString(",") { "\"$it\"" } + "]"

    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = friendlyError(e)) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // ─── Local auth fallback (when Firebase is not configured) ────────────────

    private suspend fun signInLocal(email: String, password: String) {
        val existing = userDao.getUserByEmail(email)
        if (existing == null) {
            _uiState.update {
                it.copy(isLoading = false, error = "No se encontró cuenta para este correo. Usa \"Crear una\" para registrarte.")
            }
            return
        }
        val storedHash = session.getLocalPasswordHash(email)
        if (storedHash == null || storedHash != hashPassword(password)) {
            _uiState.update { it.copy(isLoading = false, error = "Contraseña incorrecta.") }
            return
        }
        saveUserSession(existing.id, existing.nombre, existing.email)
        nextStepAfterAuth()
    }

    private suspend fun signUpLocal(email: String, password: String, name: String) {
        val existing = userDao.getUserByEmail(email)
        if (existing != null) {
            _uiState.update {
                it.copy(isLoading = false, error = "Ya existe una cuenta con este correo. Intenta iniciar sesión.")
            }
            return
        }
        val uid = UUID.randomUUID().toString()
        session.setLocalPasswordHash(email, hashPassword(password))
        saveUserSession(uid, name, email)
        nextStepAfterAuth()
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private suspend fun saveUserSession(uid: String, name: String, email: String) {
        val initials = name.trim().split(" ")
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2).joinToString("")
        val user = UserEntity(
            id = uid,
            nombre = name,
            email = email,
            avatarUrl = "",
            rol = "MEMBER",
            hogarId = ""
        )
        userDao.insertUser(user)
        firestoreRepo.syncUser(user)
        session.userId = uid
        session.userName = name
        session.userInitials = initials.ifEmpty { "U" }
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return "HF-" + (1..6).map { chars.random() }.joinToString("")
    }

    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun isFirebaseConfigError(e: Exception): Boolean {
        val msg = e.message ?: ""
        return msg.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true)
            || msg.contains("INVALID_API_KEY", ignoreCase = true)
            || msg.contains("API key not valid", ignoreCase = true)
            || (msg.contains("internal error", ignoreCase = true) && msg.contains("CONFIGURATION", ignoreCase = true))
            || msg.contains("Failed to connect", ignoreCase = true)
            || msg.contains("Unable to resolve host", ignoreCase = true)
            || msg.contains("googleapis.com", ignoreCase = true)
            || e is java.net.UnknownHostException
            || e is java.net.SocketTimeoutException
            || e.cause is java.net.UnknownHostException
            || e.cause is java.net.SocketTimeoutException
    }

    private fun friendlyError(e: Exception): String {
        val msg = e.message ?: ""
        return when {
            msg.contains("no user record", ignoreCase = true) ||
            msg.contains("user-not-found", ignoreCase = true) ->
                "No se encontró una cuenta con este correo."
            msg.contains("password is invalid", ignoreCase = true) ||
            msg.contains("wrong-password", ignoreCase = true) ->
                "Contraseña incorrecta."
            msg.contains("email address is already in use", ignoreCase = true) ||
            msg.contains("email-already-in-use", ignoreCase = true) ->
                "Ya existe una cuenta con este correo."
            msg.contains("badly formatted", ignoreCase = true) ->
                "El formato del correo es incorrecto."
            msg.contains("network", ignoreCase = true) ->
                "Sin conexión a internet. Revisa tu red e intenta de nuevo."
            msg.contains("too many requests", ignoreCase = true) ->
                "Demasiados intentos. Por favor, espera un momento e intenta de nuevo."
            else -> msg.ifEmpty { "Ocurrió un error. Por favor, intenta de nuevo." }
        }
    }
}
