package com.homeflow.app.presentation.homes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeflow.app.data.local.dao.HomeDao
import com.homeflow.app.data.local.entity.HomeEntity
import com.homeflow.app.data.remote.FirestoreRepository
import com.homeflow.app.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
            _uiState.update { it.copy(error = "Home name is required") }
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
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to create home") }
            }
        }
    }

    fun joinHome(inviteCode: String) {
        if (inviteCode.isBlank()) {
            _uiState.update { it.copy(error = "Invite code is required") }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val home = homeDao.getHomeByInviteCode(inviteCode.trim().uppercase())
                if (home == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Invalid invite code") }
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
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to join home") }
            }
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
