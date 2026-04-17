package com.homeflow.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.homeflow.app.data.local.dao.HomeDao
import com.homeflow.app.data.local.dao.TaskDao
import com.homeflow.app.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ProfileUiState(
    val userName: String = "",
    val userEmail: String = "",
    val userInitials: String = "U",
    val memberSince: String = "",
    val tasksDone: Int = 0,
    val streakDays: Int = 0,
    val score: Int = 0,
    val homeName: String = "Mi Hogar",
    val inviteCode: String = "-",
    val loggedOut: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val taskDao: TaskDao,
    private val homeDao: HomeDao,
    private val session: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val firebaseUser = auth.currentUser

        // Base info from session / Firebase
        val userName = session.userName.ifEmpty { firebaseUser?.displayName ?: "Usuario" }
        val email = firebaseUser?.email ?: ""
        val uid = session.userId.ifEmpty { firebaseUser?.uid ?: "" }
        val initials = session.userInitials.ifEmpty { "U" }

        val createdAt = firebaseUser?.metadata?.creationTimestamp
        val memberSince = if (createdAt != null) {
            "Miembro desde " + SimpleDateFormat("MMMM yyyy", Locale("es", "ES")).format(Date(createdAt))
        } else {
            "Miembro desde hace poco"
        }

        _uiState.update {
            it.copy(
                userName = userName,
                userEmail = email,
                userInitials = initials,
                memberSince = memberSince
            )
        }

        val hogarId = session.hogarId.ifEmpty { "default" }

        // Load stats from Room
        viewModelScope.launch {
            taskDao.getCompletedTaskCount(uid, hogarId).collect { count ->
                _uiState.update { it.copy(
                    tasksDone = count,
                    score = count * 10 // Simple scoring
                )}
            }
        }

        // Load home info
        viewModelScope.launch {
            homeDao.getHomeById(hogarId).collect { home ->
                if (home != null) {
                    _uiState.update { it.copy(
                        homeName = home.nombre,
                        inviteCode = home.codigoInvitacion
                    )}
                }
            }
        }
    }

    fun logOut() {
        auth.signOut()
        session.clear()
        _uiState.update { it.copy(loggedOut = true) }
    }
}
