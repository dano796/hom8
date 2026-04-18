package com.hom8.app.presentation.expenses.create

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hom8.app.data.local.dao.ActivityLogDao
import com.hom8.app.data.local.dao.ExpenseDao
import com.hom8.app.data.local.dao.HomeDao
import com.hom8.app.data.local.dao.UserDao
import com.hom8.app.data.local.entity.ActivityLogEntity
import com.hom8.app.data.local.entity.ExpenseEntity
import com.hom8.app.data.remote.FirestoreRepository
import com.hom8.app.domain.repository.UserStatsRepository
import com.hom8.app.util.SessionManager
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

enum class SplitMode {
    EQUAL,      // División equitativa
    CUSTOM      // División personalizada
}

data class CreateExpenseUiState(
    val existingExpense: ExpenseEntity? = null,
    val selectedCategory: String = "OTROS",
    val selectedDate: Long = System.currentTimeMillis(),
    val members: List<ExpenseMemberOption> = emptyList(),
    val selectedPayerId: String = "",
    val selectedParticipants: Set<String> = emptySet(), // IDs de participantes seleccionados
    val splitMode: SplitMode = SplitMode.EQUAL,
    val customAmounts: Map<String, Double> = emptyMap(), // userId -> monto personalizado
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
    private val firestoreRepo: FirestoreRepository,
    private val userStatsRepository: UserStatsRepository
) : ViewModel() {

    companion object {
        private const val TAG = "CreateExpenseViewModel"
    }

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
                    memberMap[currentUserId] = session.userName.ifEmpty { "Tú" }
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
                            name = session.userName.ifEmpty { "Yo" },
                            initials = session.userInitials.ifEmpty { "M" }
                        )
                    ) else emptyList()
                }

                val isSplitMode = home.gastosModo == "SPLIT"
                val currentPayer = _uiState.value.selectedPayerId
                    .ifEmpty { _uiState.value.existingExpense?.pagadorId ?: currentUserId }
                
                // Por defecto, todos los miembros participan en el gasto
                val currentParticipants = _uiState.value.selectedParticipants.ifEmpty {
                    options.map { it.id }.toSet()
                }
                
                _uiState.update {
                    it.copy(
                        members = options, 
                        selectedPayerId = currentPayer, 
                        selectedParticipants = currentParticipants,
                        isSplitMode = isSplitMode
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

    fun loadExpense(expenseId: String?) {
        if (expenseId == null) return
        viewModelScope.launch {
            expenseDao.getExpenseById(expenseId).collect { expense ->
                if (expense != null) {
                    // Parsear participantes del gasto existente
                    val participants = parseParticipantsJson(expense.participantes).toSet()
                    
                    _uiState.update {
                        it.copy(
                            existingExpense = expense,
                            selectedCategory = expense.categoria,
                            selectedDate = expense.fecha,
                            selectedPayerId = expense.pagadorId,
                            selectedParticipants = participants
                        )
                    }
                }
            }
        }
    }

    private fun parseParticipantsJson(json: String): List<String> {
        return parseMembersJson(json)
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

    fun toggleParticipant(userId: String) {
        _uiState.update { state ->
            val newParticipants = if (state.selectedParticipants.contains(userId)) {
                // No permitir deseleccionar si es el único participante
                if (state.selectedParticipants.size > 1) {
                    state.selectedParticipants - userId
                } else {
                    state.selectedParticipants
                }
            } else {
                state.selectedParticipants + userId
            }
            state.copy(selectedParticipants = newParticipants)
        }
    }

    fun setSplitMode(mode: SplitMode) {
        _uiState.update { it.copy(splitMode = mode) }
    }

    fun setCustomAmount(userId: String, amount: Double) {
        _uiState.update { state ->
            val newAmounts = state.customAmounts.toMutableMap()
            newAmounts[userId] = amount
            state.copy(customAmounts = newAmounts)
        }
    }

    fun saveExpense(description: String, amount: String, note: String) {
        val parsedAmount = amount.toDoubleOrNull()
        if (description.isBlank()) {
            _uiState.update { it.copy(error = "La descripción es requerida") }
            return
        }
        if (parsedAmount == null || parsedAmount <= 0) {
            _uiState.update { it.copy(error = "Ingresa un monto válido") }
            return
        }

        val state = _uiState.value
        val hogarId = session.hogarId.ifEmpty { "default" }
        val payerId = state.selectedPayerId.ifEmpty { session.userId }

        // Validar que el pagador esté entre los participantes
        val participantIds = state.selectedParticipants.toList().ifEmpty { listOf(session.userId) }
        if (!participantIds.contains(payerId)) {
            _uiState.update { it.copy(error = "El pagador debe ser uno de los participantes") }
            return
        }

        // Build participants JSON with amounts
        val participantsList = when (state.splitMode) {
            SplitMode.EQUAL -> {
                // División equitativa
                val sharePerPerson = parsedAmount / participantIds.size
                participantIds.map { userId ->
                    mapOf("userId" to userId, "amount" to sharePerPerson)
                }
            }
            SplitMode.CUSTOM -> {
                // División personalizada
                val customList = participantIds.map { userId ->
                    val customAmount = state.customAmounts[userId] ?: 0.0
                    mapOf("userId" to userId, "amount" to customAmount)
                }
                
                // Validar que la suma de montos personalizados sea igual al total
                val totalCustom = customList.sumOf { it["amount"] as Double }
                if (kotlin.math.abs(totalCustom - parsedAmount) > 0.01) {
                    _uiState.update { 
                        it.copy(error = "La suma de montos personalizados debe ser igual al total (${parsedAmount} vs ${totalCustom})") 
                    }
                    return
                }
                
                customList
            }
        }
        
        val participantes = com.google.gson.Gson().toJson(participantsList)

        viewModelScope.launch {
            try {
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
                
                // Obtener nombres de usuarios para metadata
                val payerName = state.members.find { it.id == payerId }?.name 
                    ?: if (payerId == session.userId) session.userName else "Usuario"
                
                val participantNames = state.members.associate { it.id to it.name }
                
                firestoreRepo.syncExpenseWithMetadata(expense, payerName, participantNames)

                val tipo = if (state.existingExpense != null) "EXPENSE_UPDATED" else "EXPENSE_CREATED"
                val log = ActivityLogEntity(
                    id = UUID.randomUUID().toString(),
                    hogarId = hogarId,
                    actorId = session.userId,
                    actorName = session.userName.ifEmpty { "Alguien" },
                    tipo = tipo,
                    targetTitle = description,
                    timestamp = System.currentTimeMillis()
                )
                
                activityLogDao.insertActivity(log)
                firestoreRepo.syncActivityLog(log)

                // Registrar creación de gasto en estadísticas (solo para gastos nuevos)
                if (state.existingExpense == null) {
                    userStatsRepository.onExpenseCreated(session.userId, hogarId)
                }

                _uiState.update { it.copy(isSaved = true, error = null) }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to save expense: ${e.message}", e)
                _uiState.update { it.copy(error = e.message ?: "Error al guardar el gasto") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
