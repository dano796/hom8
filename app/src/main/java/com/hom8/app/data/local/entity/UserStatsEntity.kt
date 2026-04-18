package com.hom8.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad para almacenar las estadísticas del usuario
 */
@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey
    val userId: String,

    @ColumnInfo(name = "hogar_id")
    val hogarId: String,

    @ColumnInfo(name = "tareas_completadas")
    val tareasCompletadas: Int = 0,

    @ColumnInfo(name = "racha_actual")
    val rachaActual: Int = 0,

    @ColumnInfo(name = "racha_maxima")
    val rachaMaxima: Int = 0,

    @ColumnInfo(name = "ultima_actividad")
    val ultimaActividad: Long = 0L, // Timestamp de la última actividad que cuenta para la racha

    @ColumnInfo(name = "puntuacion_total")
    val puntuacionTotal: Int = 0,

    @ColumnInfo(name = "gastos_creados")
    val gastosCreados: Int = 0,

    @ColumnInfo(name = "tareas_creadas")
    val tareasCreadas: Int = 0,

    @ColumnInfo(name = "actualizado_en")
    val actualizadoEn: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "synced")
    val synced: Int = 0
)
