package com.hom8.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hom8.app.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses WHERE hogar_id = :hogarId ORDER BY fecha DESC")
    fun getExpensesByHome(hogarId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE hogar_id = :hogarId AND categoria = :category ORDER BY fecha DESC")
    fun getExpensesByCategory(hogarId: String, category: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    fun getExpenseById(expenseId: String): Flow<ExpenseEntity?>

    @Query("SELECT * FROM expenses WHERE hogar_id = :hogarId AND fecha BETWEEN :startDate AND :endDate ORDER BY fecha DESC")
    fun getExpensesByDateRange(hogarId: String, startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpenseById(expenseId: String)
}
