package com.homeflow.app.presentation.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeflow.app.data.local.dao.HomeDao
import com.homeflow.app.data.local.dao.UserDao
import com.homeflow.app.data.local.entity.UserEntity
import com.homeflow.app.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemberItem(
    val id: String,
    val name: String,
    val initials: String,
    val email: String,
    val role: String,          // "Admin" | "Member"
    val isCurrentUser: Boolean,
    val colorIndex: Int        // for avatar color
)

data class HouseMembersUiState(
    val homeName: String = "",
    val inviteCode: String = "",
    val members: List<MemberItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HouseMembersViewModel @Inject constructor(
    private val homeDao: HomeDao,
    private val userDao: UserDao,
    private val session: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HouseMembersUiState())
    val uiState: StateFlow<HouseMembersUiState> = _uiState.asStateFlow()

    init {
        loadMembers()
    }

    private fun loadMembers() {
        val hogarId = session.hogarId.ifEmpty { return }
        val currentUserId = session.userId

        viewModelScope.launch {
            homeDao.getHomeById(hogarId).collect { home ->
                if (home == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Home not found") }
                    return@collect
                }

                val memberIds = parseMembersJson(home.miembros)
                val knownUsers = userDao.getUsersByIds(memberIds).associateBy { it.id }

                val memberItems = memberIds.mapIndexed { index, userId ->
                    val user: UserEntity? = knownUsers[userId]
                    val name = user?.nombre ?: if (userId == currentUserId) session.userName else "Member"
                    val initials = buildInitials(name)
                    MemberItem(
                        id = userId,
                        name = name,
                        initials = initials,
                        email = user?.email ?: "",
                        role = if (index == 0) "Admin" else "Member",
                        isCurrentUser = userId == currentUserId,
                        colorIndex = index % 5
                    )
                }

                _uiState.update {
                    it.copy(
                        homeName = home.nombre,
                        inviteCode = home.codigoInvitacion,
                        members = memberItems,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    private fun parseMembersJson(json: String): List<String> {
        val trimmed = json.trim().removePrefix("[").removeSuffix("]")
        if (trimmed.isBlank()) return emptyList()
        return trimmed.split(",").map { it.trim().removeSurrounding("\"") }.filter { it.isNotEmpty() }
    }

    private fun buildInitials(name: String): String =
        name.trim().split(" ")
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2).joinToString("").ifEmpty { "?" }
}
