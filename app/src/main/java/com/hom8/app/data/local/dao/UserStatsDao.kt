package com.hom8.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hom8.app.data.local.entity.UserStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {

    @Query("SELECT * FROM user_stats WHERE userId = :userId AND hogar_id = :hogarId")
    fun getUserStats(userId: String, hogarId: String): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_stats WHERE userId = :userId AND hogar_id = :hogarId")
    suspend fun getUserStatsSync(userId: String, hogarId: String): UserStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStatsEntity)

    @Update
    suspend fun updateUserStats(stats: UserStatsEntity)

    @Query("""
        UPDATE user_stats 
        SET tareas_completadas = tareas_completadas + 1,
            puntuacion_total = puntuacion_total + :puntos,
            actualizado_en = :timestamp,
            synced = 0
        WHERE userId = :userId AND hogar_id = :hogarId
    """)
    suspend fun incrementTasksCompleted(userId: String, hogarId: String, puntos: Int, timestamp: Long)

    @Query("""
        UPDATE user_stats 
        SET gastos_creados = gastos_creados + 1,
            puntuacion_total = puntuacion_total + :puntos,
            actualizado_en = :timestamp,
            synced = 0
        WHERE userId = :userId AND hogar_id = :hogarId
    """)
    suspend fun incrementExpensesCreated(userId: String, hogarId: String, puntos: Int, timestamp: Long)

    @Query("""
        UPDATE user_stats 
        SET tareas_creadas = tareas_creadas + 1,
            actualizado_en = :timestamp,
            synced = 0
        WHERE userId = :userId AND hogar_id = :hogarId
    """)
    suspend fun incrementTasksCreated(userId: String, hogarId: String, timestamp: Long)

    @Query("""
        UPDATE user_stats 
        SET racha_actual = :nuevaRacha,
            racha_maxima = CASE WHEN :nuevaRacha > racha_maxima THEN :nuevaRacha ELSE racha_maxima END,
            ultima_actividad = :timestamp,
            puntuacion_total = puntuacion_total + :puntosBonus,
            actualizado_en = :timestamp,
            synced = 0
        WHERE userId = :userId AND hogar_id = :hogarId
    """)
    suspend fun updateStreak(userId: String, hogarId: String, nuevaRacha: Int, puntosBonus: Int, timestamp: Long)

    @Query("DELETE FROM user_stats WHERE userId = :userId AND hogar_id = :hogarId")
    suspend fun deleteUserStats(userId: String, hogarId: String)

    @Query("SELECT * FROM user_stats WHERE hogar_id = :hogarId ORDER BY puntuacion_total DESC")
    fun getLeaderboard(hogarId: String): Flow<List<UserStatsEntity>>
}
