package com.hom8.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hom8.app.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    @Query("SELECT * FROM payments WHERE hogar_id = :hogarId ORDER BY fecha DESC")
    fun getPaymentsByHome(hogarId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE (from_user_id = :userId OR to_user_id = :userId) AND hogar_id = :hogarId ORDER BY fecha DESC")
    fun getPaymentsByUser(userId: String, hogarId: String): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)
}
