package com.hom8.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "titulo")
    val titulo: String,

    @ColumnInfo(name = "descripcion")
    val descripcion: String = "",

    @ColumnInfo(name = "responsable_id")
    val responsableId: String = "",

    @ColumnInfo(name = "creado_por")
    val creadoPor: String = "",

    @ColumnInfo(name = "hogar_id")
    val hogarId: String = "",

    @ColumnInfo(name = "fecha_limite")
    val fechaLimite: Long? = null,

    @ColumnInfo(name = "prioridad")
    val prioridad: String = "MEDIA",

    @ColumnInfo(name = "estado")
    val estado: String = "PENDIENTE",

    @ColumnInfo(name = "recurrencia")
    val recurrencia: String? = null,

    @ColumnInfo(name = "etiquetas")
    val etiquetas: String = "[]",

    @ColumnInfo(name = "checklist")
    val checklist: String = "[]",

    @ColumnInfo(name = "comentarios")
    val comentarios: String = "[]",

    @ColumnInfo(name = "actividad")
    val actividad: String = "[]",

    @ColumnInfo(name = "adjuntos")
    val adjuntos: String = "[]",

    @ColumnInfo(name = "creado_en")
    val creadoEn: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "actualizado_en")
    val actualizadoEn: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "synced")
    val synced: Int = 0
)
