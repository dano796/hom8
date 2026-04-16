package com.homeflow.app.presentation.expenses.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeflow.app.data.local.dao.ActivityLogDao
import com.homeflow.app.data.local.dao.ExpenseDao
import com.homeflow.app.data.local.dao.HomeDao
import com.homeflow.app.data.local.dao.UserDao
import com.homeflow.app.data.local.entity.ActivityLogEntity
import com.homeflow.app.data.local.entity.ExpenseEntity
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

data class ExpenseMemberOption(
    val id: String,
    val name: String,
    val initials: String
)

data class CreateExpenseUiState(
    val existingExpense: ExpenseEntity? = null,
    val selectedCategory: String = "OTHER",
    val selectedDate: Long = System.currentTimeMillis(),
    val members: List<ExpenseMemberOption> = emptyList(),
    val selectedPayerId: String = "",
    val isSplitMode: Boolean = true,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateExpenseViewModel @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val homeDao: HomeDao,
    private val userDao: UserDao,
    private val activityLogDao: ActivityLogDao,
    private val session: SessionManager,
    private val firestoreRepo: FirestoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateExpenseUiState())
    val uiState: StateFlow<CreateExpenseUiState> = _uiState.asStateFlow()

    init {
        loadMembers()
    }

    private fun loadMembers() {
        val hogarId = session.hogarId.ifEmpty { return }
        val currentUserId = session.userId
        viewModelScope.launch {
            homeDao.getHomeById(hogarId).collect { home ->
                if (home == null) return@collect
                val memberIds = parseMembersJson(home.miembros)
                val users = userDao.getUsersByIds(memberIds)
                val memberMap = users.associate { it.id to it.nombre }.toMutableMap()
                if (currentUserId.isNotEmpty()) {
                    memberMap[currentUserId] = session.userName.ifEmpty { "You" }
                }
                val options = memberIds.mapNotNull { id ->
                    val name = memberMap[id] ?: return@mapNotNull null
                    ExpenseMemberOption(
                        id = id,
                        name = name,
                        initials = name.split(" ")
                            .filter { it.isNotBlank() }
                            .take(2)
                            .joinToString("") { it.first().uppercaseChar().toString() }
                            .ifEmpty { "?" }
                    )
                }.ifEmpty {
                    // Fallback: at least the current user
                    if (currentUserId.isNotEmpty()) listOf(
                        ExpenseMemberOption(
                            id = currentUserId,
                            name = session.userName.ifEmpty { "Me" },
                            initials = session.userInitials.ifEmpty { "M" }
                        )
                    ) else emptyList()
                }

                val isSplitMode = home.gastosModo == "SPLIT"
                val currentPayer = _uiState.value.selectedPayerId
                    .ifEmpty { _uiState.value.existingExpense?.pagadorId ?: currentUserId }
                _uiState.update {
                    it.copy(members = options, selectedPayerId = currentPayer, isSplitMode = isSplitMode)
                }
            }
        }
    }

    private fun parseMembersJson(json: String): List<String> {
        val trimmed = json.trim().removePrefix("[").removeSuffix("]")
        if (trimmed.isBlank()) return emptyList()
        return trimmed.split(",").map { it.trim().removeSurrounding("\"") }.filter { it.isNotEmpty() }
    }

    fun loadExpense(expenseId: String?) {
        if (expenseId == null) return
        viewModelScope.launch {
            expenseDao.getExpenseById(expenseId).collect { expense ->
                if (expense != null) {
                    _uiState.update {
                        it.copy(
                            existingExpense = expense,
                            selectedCategory = expense.categoria,
                            selectedDate = expense.fecha,
                            selectedPayerId = expense.pagadorId
                        )
                    }
                }
            }
        }
    }

    fun setCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setDate(timestamp: Long) {
        _uiState.update { it.copy(selectedDate = timestamp) }
    }

    fun setPayerId(userId: String) {
        _uiState.update { it.copy(selectedPayerId = userId) }
    }

    fun saveExpense(description: String, amount: String, note: String) {
        val parsedAmount = amount.toDoubleOrNull()
        if (description.isBlank()) {
            _uiState.update { it.copy(error = "Description is required") }
            return
        }
        if (parsedAmount == null || parsedAmount <= 0) {
            _uiState.update { it.copy(error = "Enter a valid amount") }
            return
        }

        val state = _uiState.value
        val hogarId = session.hogarId.ifEmpty { "default" }
        val payerId = state.selectedPayerId.ifEmpty { session.userId }

        // Build participants JSON from all loaded members
        val participantIds = state.members.map { it.id }.ifEmpty { listOf(session.userId) }
        val participantes = "[" + participantIds.joinToString(",") { "\"$it\"" } + "]"

        viewModelScope.launch {
            val expense = state.existingExpense?.copy(
                descripcion = description,
                monto = parsedAmount,
                categoria = state.selectedCategory,
                pagadorId = payerId,
                fecha = state.selectedDate,
                nota = note,
                participantes = participantes,
                synced = 0
            ) ?: ExpenseEntity(
                id = UUID.randomUUID().toString(),
                descripcion = description,
                monto = parsedAmount,
                categoria = state.selectedCategory,
                pagadorId = payerId,
                hogarId = hogarId,
                fecha = state.selectedDate,
                nota = note,
                participantes = participantes,
                synced = 0
            )
            expenseDao.insertExpense(expense)
            firestoreRepo.syncExpense(expense)

            val tipo = if (state.existingExpense != null) "EXPENSE_UPDATED" else "EXPENSE_CREATED"
            val log = ActivityLogEntity(
                id = UUID.randomUUID().toString(),
                hogarId = hogarId,
                actorId = session.userId,
                actorName = session.userName.ifEmpty { "Someone" },
                tipo = tipo,
                targetTitle = description,
                timestamp = System.currentTimeMillis()
            )
            activityLogDao.insertActivity(log)
            firestoreRepo.syncActivityLog(log)

            _uiState.update { it.copy(isSaved = true, error = null) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
