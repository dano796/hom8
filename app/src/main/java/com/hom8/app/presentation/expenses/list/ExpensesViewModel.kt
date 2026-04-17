package com.hom8.app.presentation.expenses.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hom8.app.data.local.dao.ActivityLogDao
import com.hom8.app.data.local.dao.ExpenseDao
import com.hom8.app.data.local.dao.HomeDao
import com.hom8.app.data.local.dao.UserDao
import com.hom8.app.data.local.entity.ActivityLogEntity
import com.hom8.app.data.local.entity.ExpenseEntity
import com.hom8.app.data.remote.FirestoreRepository
import com.hom8.app.util.SessionManager
import java.util.UUID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ExpenseCategoryFilter(val key: String) {
    ALL(""),
    COMIDA("COMIDA"),
    SUPERMERCADO("SUPERMERCADO"),
    SERVICIOS("SERVICIOS"),
    TRANSPORTE("TRANSPORTE"),
    OCIO("OCIO")
}

data class ExpensesUiState(
    val expenses: List<ExpenseEntity> = emptyList(),
    val categoryFilter: ExpenseCategoryFilter = ExpenseCategoryFilter.ALL,
    val theyOweAmount: Double = 0.0,
    val youOweAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    /** userId → display name for all home members */
    val membersMap: Map<String, String> = emptyMap(),
    /** true = SPLIT mode (divide + balance); false = TRACK mode (log only) */
    val isSplitMode: Boolean = true,
    /** true if the current user is the home admin (first member) */
    val isAdmin: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val homeDao: HomeDao,
    private val userDao: UserDao,
    private val activityLogDao: ActivityLogDao,
    private val session: SessionManager,
    private val firestoreRepo: FirestoreRepository
) : ViewModel() {

    private val _membersMap = MutableStateFlow<Map<String, String>>(emptyMap())
    private val _splitMode = MutableStateFlow(true)
    private val _isAdmin = MutableStateFlow(false)

    private val _categoryFilter = MutableStateFlow(ExpenseCategoryFilter.ALL)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _expenses = _categoryFilter.flatMapLatest { filter ->
        val hogarId = session.hogarId.ifEmpty { "default" }
        if (filter == ExpenseCategoryFilter.ALL) {
            expenseDao.getExpensesByHome(hogarId)
        } else {
            expenseDao.getExpensesByCategory(hogarId, filter.key)
        }
    }

    private val _balanceState = MutableStateFlow(Pair(0.0, 0.0)) // theyOwe, youOwe

    val uiState: StateFlow<ExpensesUiState> = combine(
        _expenses,
        _categoryFilter,
        _balanceState,
        _membersMap,
        combine(_splitMode, _isAdmin) { split, admin -> Pair(split, admin) }
    ) { expenses: List<ExpenseEntity>,
        filter: ExpenseCategoryFilter,
        balancePair: Pair<Double, Double>,
        membersMap: Map<String, String>,
        (isSplitMode, isAdmin): Pair<Boolean, Boolean> ->
        ExpensesUiState(
            expenses = expenses,
            categoryFilter = filter,
            theyOweAmount = balancePair.first,
            youOweAmount = balancePair.second,
            totalAmount = expenses.sumOf { it.monto },
            membersMap = membersMap,
            isSplitMode = isSplitMode,
            isAdmin = isAdmin,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExpensesUiState()
    )

    init {
        computeBalances()
        loadMembers()
    }

    private fun loadMembers() {
        val hogarId = session.hogarId.ifEmpty { return }
        val currentUserId = session.userId
        viewModelScope.launch {
            homeDao.getHomeById(hogarId).collect { home ->
                if (home == null) return@collect
                val memberIds = parseMembersJson(home.miembros)

                // Admin is the first member in the list
                _isAdmin.value = memberIds.firstOrNull() == currentUserId
                // Mode comes from the home entity
                _splitMode.value = home.gastosModo == "SPLIT"

                val users = userDao.getUsersByIds(memberIds)
                val map = users.associate { it.id to it.nombre }.toMutableMap()
                if (currentUserId.isNotEmpty()) {
                    map[currentUserId] = session.userName.ifEmpty { "Tú" }
                }
                _membersMap.value = map
            }
        }
    }

    private fun parseMembersJson(json: String): List<String> {
        val trimmed = json.trim().removePrefix("[").removeSuffix("]")
        if (trimmed.isBlank()) return emptyList()
        return trimmed.split(",").map { it.trim().removeSurrounding("\"") }.filter { it.isNotEmpty() }
    }

    private fun computeBalances() {
        viewModelScope.launch {
            val hogarId = session.hogarId.ifEmpty { "default" }
            val userId = session.userId
            expenseDao.getExpensesByHome(hogarId).collect { expenses ->
                var theyOwe = 0.0
                var youOwe = 0.0
                expenses.forEach { expense ->
                    val participantCount = parseParticipantCount(expense.participantes)
                    val share = if (participantCount > 0) expense.monto / participantCount else 0.0
                    if (expense.pagadorId == userId) {
                        // You paid — others owe you (minus your own share)
                        theyOwe += expense.monto - share
                    } else {
                        // Someone else paid — you owe your share
                        youOwe += share
                    }
                }
                _balanceState.value = Pair(theyOwe, youOwe)
            }
        }
    }

    fun setCategoryFilter(filter: ExpenseCategoryFilter) {
        _categoryFilter.update { filter }
    }

    fun setExpensesMode(splitMode: Boolean) {
        if (!_isAdmin.value) return  // only admin can change this
        val hogarId = session.hogarId.ifEmpty { return }
        viewModelScope.launch {
            val home = homeDao.getHomeById(hogarId).first() ?: return@launch
            homeDao.updateHome(home.copy(gastosModo = if (splitMode) "SPLIT" else "TRACK"))
            // _splitMode will update automatically via loadMembers() collector
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            expenseDao.deleteExpense(expense)
            firestoreRepo.deleteExpense(expense.hogarId, expense.id)
            val log = ActivityLogEntity(
                id = UUID.randomUUID().toString(),
                hogarId = expense.hogarId,
                actorId = session.userId,
                actorName = session.userName.ifEmpty { "Alguien" },
                tipo = "EXPENSE_DELETED",
                targetTitle = expense.descripcion,
                timestamp = System.currentTimeMillis()
            )
            activityLogDao.insertActivity(log)
            firestoreRepo.syncActivityLog(log)
        }
    }

    private fun parseParticipantCount(participantesJson: String): Int {
        return try {
            // Simple count of items in JSON array "["a","b"]" → 2
            if (participantesJson == "[]") 1
            else participantesJson.split(",").size
        } catch (_: Exception) {
            1
        }
    }
}
