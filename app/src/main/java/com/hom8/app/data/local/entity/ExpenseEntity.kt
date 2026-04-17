package com.hom8.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "descripcion")
    val descripcion: String,

    @ColumnInfo(name = "monto")
    val monto: Double,

    @ColumnInfo(name = "categoria")
    val categoria: String,

    @ColumnInfo(name = "pagador_id")
    val pagadorId: String,

    @ColumnInfo(name = "hogar_id")
    val hogarId: String,

    @ColumnInfo(name = "fecha")
    val fecha: Long,

    @ColumnInfo(name = "nota")
    val nota: String = "",

    @ColumnInfo(name = "participantes")
    val participantes: String = "[]",

    @ColumnInfo(name = "synced")
    val synced: Int = 0
)
