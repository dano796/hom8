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
import java.text.NumberFormat
import java.util.Locale
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
    private val session: SessionManager,
    private val firestoreRepository: com.hom8.app.data.remote.FirestoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BalancesUiState())
    val uiState: StateFlow<BalancesUiState> = _uiState
    
    private val currencyFmt = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    init {
        loadBalances()
    }

    private fun loadBalances() {
        val hogarId = session.hogarId.ifEmpty { "default" }
        val userId = session.userId
        val userName = session.userName.ifEmpty { "Tú" }

        android.util.Log.d("BalancesViewModel", "🏠 Cargando balances para hogar: $hogarId")
        android.util.Log.d("BalancesViewModel", "👤 Usuario actual: $userId ($userName)")

        viewModelScope.launch {
            // Load members once — home members rarely change mid-session
            val membersMap = mutableMapOf<String, String>()
            membersMap[userId] = userName
            
            val home = homeDao.getHomeById(hogarId).first()
            android.util.Log.d("BalancesViewModel", "🏡 Home encontrado: ${home?.nombre}")
            
            if (home != null) {
                android.util.Log.d("BalancesViewModel", "📋 Miembros JSON: ${home.miembros}")
                val memberIds = parseMembersJson(home.miembros)
                android.util.Log.d("BalancesViewModel", "👥 IDs de miembros parseados: $memberIds")
                
                val users = userDao.getUsersByIds(memberIds)
                android.util.Log.d("BalancesViewModel", "👥 Usuarios obtenidos de DB: ${users.size}")
                users.forEach { user ->
                    android.util.Log.d("BalancesViewModel", "   - ${user.id}: ${user.nombre}")
                }
                
                membersMap.putAll(users.associate { it.id to it.nombre })
                membersMap[userId] = userName // always prefer session name for self
                
                android.util.Log.d("BalancesViewModel", "📝 MembersMap final: $membersMap")
            }

            combine(
                expenseDao.getExpensesByHome(hogarId),
                paymentDao.getPaymentsByHome(hogarId)
            ) { expenses, payments ->
                    // net[memberId] = positive means they owe me, negative means I owe them
                    val net = mutableMapOf<String, Double>()

                    expenses.forEach { expense ->
                        android.util.Log.d("BalancesViewModel", "💰 Procesando gasto: ${expense.descripcion}")
                        android.util.Log.d("BalancesViewModel", "   Pagador: ${expense.pagadorId}")
                        android.util.Log.d("BalancesViewModel", "   Participantes JSON: ${expense.participantes}")
                        
                        val participantsData = parseParticipantsWithAmounts(expense.participantes, expense.monto)
                        android.util.Log.d("BalancesViewModel", "   Participantes parseados: $participantsData")
                        
                        if (participantsData.isNotEmpty()) {
                            // Nuevo formato con montos o formato antiguo procesado
                            if (expense.pagadorId == userId) {
                                // Current user paid — every other participant owes their assigned share
                                participantsData.filter { it.first != userId }.forEach { (pid, amount) ->
                                    android.util.Log.d("BalancesViewModel", "   → $pid me debe $amount")
                                    net[pid] = (net[pid] ?: 0.0) + amount
                                }
                            } else {
                                // Someone else paid — find my share and I owe it to the payer
                                val myShare = participantsData.find { it.first == userId }?.second
                                if (myShare != null && myShare > 0) {
                                    android.util.Log.d("BalancesViewModel", "   → Yo debo $myShare a ${expense.pagadorId}")
                                    net[expense.pagadorId] = (net[expense.pagadorId] ?: 0.0) - myShare
                                }
                            }
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

                    android.util.Log.d("BalancesViewModel", "📊 Calculando balances para usuario: $userId")
                    android.util.Log.d("BalancesViewModel", "   Balances netos calculados: ${net.size} usuarios")

                    net.forEach { (otherId, amount) ->
                        val otherName = membersMap[otherId] ?: otherId.take(8).ifEmpty { "Miembro" }
                        android.util.Log.d("BalancesViewModel", "   - $otherId ($otherName): $amount")
                        android.util.Log.d("BalancesViewModel", "   - otherName type: ${otherName::class.simpleName}, value: '$otherName'")
                        
                        when {
                            amount > 0.01 -> {
                                // They owe me
                                theyOwe += amount
                                val debtItem = DebtItem(
                                    fromUserId = otherId,
                                    toUserId = userId,
                                    fromName = otherName,
                                    toName = userName,
                                    amount = amount,
                                    isYouDebtor = false
                                )
                                android.util.Log.d("BalancesViewModel", "   ✅ Creando DebtItem: fromName='${debtItem.fromName}', toName='${debtItem.toName}'")
                                debts.add(debtItem)
                                android.util.Log.d("BalancesViewModel", "   ✅ $otherName te debe: ${currencyFmt.format(amount)}")
                            }
                            amount < -0.01 -> {
                                // I owe them
                                val owedAmount = -amount
                                youOwe += owedAmount
                                val debtItem = DebtItem(
                                    fromUserId = userId,
                                    toUserId = otherId,
                                    fromName = userName,
                                    toName = otherName,
                                    amount = owedAmount,
                                    isYouDebtor = true
                                )
                                android.util.Log.d("BalancesViewModel", "   ❌ Creando DebtItem: fromName='${debtItem.fromName}', toName='${debtItem.toName}'")
                                debts.add(debtItem)
                                android.util.Log.d("BalancesViewModel", "   ❌ Tú debes a $otherName: ${currencyFmt.format(owedAmount)}")
                            }
                        }
                    }

                    val netAmount = theyOwe - youOwe
                    android.util.Log.d("BalancesViewModel", "📈 Resumen:")
                    android.util.Log.d("BalancesViewModel", "   Te deben: ${currencyFmt.format(theyOwe)}")
                    android.util.Log.d("BalancesViewModel", "   Tú debes: ${currencyFmt.format(youOwe)}")
                    android.util.Log.d("BalancesViewModel", "   Balance neto: ${currencyFmt.format(netAmount)}")
                    android.util.Log.d("BalancesViewModel", "   Deudas pendientes: ${debts.size}")

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

    fun recordPayment(debt: DebtItem, amount: Double, note: String) {
        viewModelScope.launch {
            val hogarId = session.hogarId.ifEmpty { "default" }
            val payment = PaymentEntity(
                id = UUID.randomUUID().toString(),
                fromUserId = debt.fromUserId,
                toUserId = debt.toUserId,
                monto = amount,
                fecha = System.currentTimeMillis(),
                nota = note.ifEmpty { "Pago registrado" },
                hogarId = hogarId,
                synced = 0
            )
            paymentDao.insertPayment(payment)
            
            // Sincronizar con Firestore
            firestoreRepository.syncPayment(payment)
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

    private fun parseParticipantsWithAmounts(json: String, totalAmount: Double): List<Pair<String, Double>> {
        android.util.Log.d("BalancesViewModel", "🔍 Parseando participantes: $json")
        
        return try {
            // Intentar parsear como lista de objetos
            val type = object : com.google.gson.reflect.TypeToken<List<Map<String, Any>>>() {}.type
            val list: List<Map<String, Any>> = com.google.gson.Gson().fromJson(json, type) ?: emptyList()
            
            android.util.Log.d("BalancesViewModel", "   Lista parseada: $list")
            
            // Verificar si es el formato NUEVO (tiene "userId" y "amount")
            if (list.isNotEmpty() && list[0].containsKey("userId") && list[0].containsKey("amount")) {
                android.util.Log.d("BalancesViewModel", "   ✅ Formato NUEVO detectado (con userId y amount)")
                val result = list.mapNotNull { map ->
                    val userId = map["userId"] as? String ?: return@mapNotNull null
                    val amount = when (val amt = map["amount"]) {
                        is Double -> amt
                        is Number -> amt.toDouble()
                        else -> return@mapNotNull null
                    }
                    userId to amount
                }
                android.util.Log.d("BalancesViewModel", "   Resultado: $result")
                result
            } 
            // Verificar si tiene "userId" pero NO "amount" (formato con metadata pero sin montos)
            else if (list.isNotEmpty() && list[0].containsKey("userId")) {
                android.util.Log.d("BalancesViewModel", "   ✅ Formato con userId pero sin amount - dividiendo equitativamente")
                // Extraer solo los userIds
                val participantIds = list.mapNotNull { it["userId"] as? String }
                android.util.Log.d("BalancesViewModel", "   UserIds extraídos: $participantIds")
                val count = participantIds.size.coerceAtLeast(1)
                val share = totalAmount / count
                val result = participantIds.map { it to share }
                android.util.Log.d("BalancesViewModel", "   Resultado: $result")
                result
            }
            else {
                android.util.Log.d("BalancesViewModel", "   ⚠️ Formato ANTIGUO detectado (solo IDs como strings)")
                // Es formato antiguo: array de strings simples ["userId1", "userId2"]
                val participantIds = parseParticipantsJson(json)
                android.util.Log.d("BalancesViewModel", "   IDs parseados: $participantIds")
                val count = participantIds.size.coerceAtLeast(1)
                val share = totalAmount / count
                val result = participantIds.map { it to share }
                android.util.Log.d("BalancesViewModel", "   Resultado: $result")
                result
            }
        } catch (e: Exception) {
            android.util.Log.e("BalancesViewModel", "   ❌ Error parseando: ${e.message}", e)
            // Fallback: formato antiguo
            val participantIds = parseParticipantsJson(json)
            val count = participantIds.size.coerceAtLeast(1)
            val share = totalAmount / count
            participantIds.map { it to share }
        }
    }
}
