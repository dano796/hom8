package com.homeflow.app.domain.repository

import com.homeflow.app.domain.model.Balance
import com.homeflow.app.domain.model.Deuda
import com.homeflow.app.domain.model.Gasto
import com.homeflow.app.domain.model.Pago
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getExpensesByHome(hogarId: String): Flow<List<Gasto>>
    fun getExpensesByCategory(hogarId: String, category: String): Flow<List<Gasto>>
    fun getExpenseById(expenseId: String): Flow<Gasto?>
    fun getExpensesByDateRange(hogarId: String, startDate: Long, endDate: Long): Flow<List<Gasto>>
    fun getPaymentsByHome(hogarId: String): Flow<List<Pago>>

    suspend fun createExpense(gasto: Gasto): Result<Gasto>
    suspend fun updateExpense(gasto: Gasto): Result<Unit>
    suspend fun deleteExpense(expenseId: String): Result<Unit>
    suspend fun recordPayment(pago: Pago): Result<Unit>
    suspend fun calculateBalances(hogarId: String): List<Deuda>
    suspend fun getUserBalance(userId: String, hogarId: String): Balance
}
