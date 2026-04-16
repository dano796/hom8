package com.homeflow.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "homes")
data class HomeEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "codigo_invitacion")
    val codigoInvitacion: String = "",

    @ColumnInfo(name = "miembros")
    val miembros: String = "[]",

    @ColumnInfo(name = "creado_en")
    val creadoEn: Long = System.currentTimeMillis(),

    /** "SPLIT" = divide costs + track balances | "TRACK" = log only, no splitting */
    @ColumnInfo(name = "gastos_modo")
    val gastosModo: String = "SPLIT",

    @ColumnInfo(name = "synced")
    val synced: Int = 0
)
