package com.hom8.app.presentation.homes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.hom8.app.data.local.dao.HomeDao
import com.hom8.app.data.local.entity.HomeEntity
import com.hom8.app.data.remote.FirestoreRepository
import com.hom8.app.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

data class AddHomeUiState(
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddHomeViewModel @Inject constructor(
    private val homeDao: HomeDao,
    private val session: SessionManager,
    private val firestoreRepo: FirestoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddHomeUiState())
    val uiState: StateFlow<AddHomeUiState> = _uiState.asStateFlow()

    fun createHome(homeName: String) {
        val userId = session.userId
        if (homeName.isBlank()) {
            _uiState.update { it.copy(error = "El nombre del hogar es requerido") }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val homeId = UUID.randomUUID().toString()
                val code = generateInviteCode()
                val home = HomeEntity(
                    id = homeId,
                    nombre = homeName.trim(),
                    codigoInvitacion = code,
                    miembros = "[\"$userId\"]",
                    creadoEn = System.currentTimeMillis()
                )
                homeDao.insertHome(home)
                firestoreRepo.syncHome(home)
                session.hogarId = homeId
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al crear el hogar") }
            }
        }
    }

    fun joinHome(inviteCode: String) {
        if (inviteCode.isBlank()) {
            _uiState.update { it.copy(error = "El código de invitación es requerido") }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // 1. Try to find the home in local database first
                var home = homeDao.getHomeByInviteCode(inviteCode.trim().uppercase())
                
                // 2. If not found locally, search in Firestore
                if (home == null) {
                    home = fetchHomeFromFirestoreByCode(inviteCode.trim().uppercase())
                }
                
                if (home == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Código de invitación inválido") }
                    return@launch
                }
                
                val userId = session.userId
                val current = parseMembersJson(home.miembros)
                if (!current.contains(userId)) {
                    val updated = home.copy(miembros = buildMembersJson(current + userId))
                    homeDao.updateHome(updated)
                    firestoreRepo.syncHome(updated)
                }
                session.hogarId = home.id
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
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
                .whereEqualTo("codigoInvitacion", inviteCode)
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

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return "HF-" + (1..6).map { chars.random() }.joinToString("")
    }

    private fun parseMembersJson(json: String): List<String> {
        val trimmed = json.trim().removePrefix("[").removeSuffix("]")
        if (trimmed.isBlank()) return emptyList()
        return trimmed.split(",").map { it.trim().removeSurrounding("\"") }.filter { it.isNotEmpty() }
    }

    private fun buildMembersJson(members: List<String>): String =
        "[" + members.joinToString(",") { "\"$it\"" } + "]"
}
