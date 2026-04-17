package com.hom8.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "from_user_id")
    val fromUserId: String,

    @ColumnInfo(name = "to_user_id")
    val toUserId: String,

    @ColumnInfo(name = "monto")
    val monto: Double,

    @ColumnInfo(name = "fecha")
    val fecha: Long,

    @ColumnInfo(name = "nota")
    val nota: String = "",

    @ColumnInfo(name = "hogar_id")
    val hogarId: String,

    @ColumnInfo(name = "synced")
    val synced: Int = 0
)
