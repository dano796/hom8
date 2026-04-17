package com.hom8.app.presentation.expenses.balances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hom8.app.data.local.dao.ExpenseDao
import com.hom8.app.data.local.dao.HomeDao
import com.hom8.app.data.local.dao.PaymentDao
import com.hom8.app.data.local.dao.UserDao
import com.hom8.app.data.local.entity.PaymentEntity
import com.hom8.app.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class DebtItem(
    val fromUserId: String,
    val toUserId: String,
    val fromName: String,
    val toName: String,
    val amount: Double,
    val isYouDebtor: Boolean
)

data class BalancesUiState(
    val netAmount: Double = 0.0,
    val theyOweAmount: Double = 0.0,
    val youOweAmount: Double = 0.0,
    val pendingDebts: List<DebtItem> = emptyList(),
    val settledPayments: List<PaymentEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class BalancesViewModel @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val paymentDao: PaymentDao,
    private val homeDao: HomeDao,
    private val userDao: UserDao,
    private val session: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BalancesUiState())
    val uiState: StateFlow<BalancesUiState> = _uiState

    init {
        loadBalances()
    }

    private fun loadBalances() {
        val hogarId = session.hogarId.ifEmpty { "default" }
        val userId = session.userId
        val userName = session.userName.ifEmpty { "Tú" }

        viewModelScope.launch {
            // Load members once — home members rarely change mid-session
            val membersMap = mutableMapOf<String, String>()
            membersMap[userId] = userName
            val home = homeDao.getHomeById(hogarId).first()
            if (home != null) {
                val memberIds = parseMembersJson(home.miembros)
                val users = userDao.getUsersByIds(memberIds)
                membersMap.putAll(users.associate { it.id to it.nombre })
                membersMap[userId] = userName // always prefer session name for self
            }

            combine(
                expenseDao.getExpensesByHome(hogarId),
                paymentDao.getPaymentsByHome(hogarId)
            ) { expenses, payments ->
                    // net[memberId] = positive means they owe me, negative means I owe them
                    val net = mutableMapOf<String, Double>()

                    expenses.forEach { expense ->
                        val participantIds = parseParticipantsJson(expense.participantes)
                        val count = participantIds.size.coerceAtLeast(1)
                        val share = expense.monto / count

                        if (expense.pagadorId == userId) {
                            // Current user paid — every other participant owes their share
                            participantIds.filter { it != userId }.forEach { pid ->
                                net[pid] = (net[pid] ?: 0.0) + share
                            }
                        } else if (participantIds.contains(userId)) {
                            // Someone else paid and I'm a participant — I owe my share to the payer
                            net[expense.pagadorId] = (net[expense.pagadorId] ?: 0.0) - share
                        }
                    }

                    // Apply settled payments
                    payments.forEach { payment ->
                        if (payment.fromUserId == userId) {
                            // I paid someone → reduce what I owe them (negative net)
                            net[payment.toUserId] = (net[payment.toUserId] ?: 0.0) + payment.monto
                        } else if (payment.toUserId == userId) {
                            // Someone paid me → reduce what they owe me (positive net)
                            net[payment.fromUserId] = (net[payment.fromUserId] ?: 0.0) - payment.monto
                        }
                    }

                    var theyOwe = 0.0
                    var youOwe = 0.0
                    val debts = mutableListOf<DebtItem>()

                    net.forEach { (otherId, amount) ->
                        val otherName = membersMap[otherId] ?: otherId.take(8).ifEmpty { "Miembro" }
                        when {
                            amount > 0.01 -> {
                                // They owe me
                                theyOwe += amount
                                debts.add(DebtItem(
                                    fromUserId = otherId,
                                    toUserId = userId,
                                    fromName = otherName,
                                    toName = userName,
                                    amount = amount,
                                    isYouDebtor = false
                                ))
                            }
                            amount < -0.01 -> {
                                // I owe them
                                val owedAmount = -amount
                                youOwe += owedAmount
                                debts.add(DebtItem(
                                    fromUserId = userId,
                                    toUserId = otherId,
                                    fromName = userName,
                                    toName = otherName,
                                    amount = owedAmount,
                                    isYouDebtor = true
                                ))
                            }
                        }
                    }

                    val netAmount = theyOwe - youOwe

                    BalancesUiState(
                        netAmount = netAmount,
                        theyOweAmount = theyOwe,
                        youOweAmount = youOwe,
                        pendingDebts = debts.sortedByDescending { it.amount },
                        settledPayments = payments.sortedByDescending { it.fecha },
                        isLoading = false
                    )
                }.collect { state ->
                    _uiState.value = state
                }
        }
    }

    fun recordPayment(debt: DebtItem) {
        viewModelScope.launch {
            val hogarId = session.hogarId.ifEmpty { "default" }
            val payment = PaymentEntity(
                id = UUID.randomUUID().toString(),
                fromUserId = debt.fromUserId,
                toUserId = debt.toUserId,
                monto = debt.amount,
                fecha = System.currentTimeMillis(),
                nota = "Saldo liquidado",
                hogarId = hogarId,
                synced = 0
            )
            paymentDao.insertPayment(payment)
        }
    }

    private fun parseMembersJson(json: String): List<String> {
        val trimmed = json.trim().removePrefix("[").removeSuffix("]")
        if (trimmed.isBlank()) return emptyList()
        return trimmed.split(",").map { it.trim().removeSurrounding("\"") }.filter { it.isNotEmpty() }
    }

    private fun parseParticipantsJson(json: String): List<String> {
        return parseMembersJson(json)
    }
}
