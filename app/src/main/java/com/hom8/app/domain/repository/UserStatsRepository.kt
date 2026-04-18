package com.hom8.app.domain.repository

import com.hom8.app.data.local.dao.UserStatsDao
import com.hom8.app.data.local.entity.UserStatsEntity
import com.hom8.app.data.remote.FirestoreRepository
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar las estadísticas del usuario
 */
@Singleton
class UserStatsRepository @Inject constructor(
    private val userStatsDao: UserStatsDao,
    private val firestoreRepository: FirestoreRepository
) {

    fun getUserStats(userId: String, hogarId: String): Flow<UserStatsEntity?> {
        return userStatsDao.getUserStats(userId, hogarId)
    }

    suspend fun getUserStatsSync(userId: String, hogarId: String): UserStatsEntity? {
        return userStatsDao.getUserStatsSync(userId, hogarId)
    }

    /**
     * Inicializa las estadísticas de un usuario si no existen
     */
    suspend fun initializeUserStats(userId: String, hogarId: String) {
        val existing = userStatsDao.getUserStatsSync(userId, hogarId)
        if (existing == null) {
            val newStats = UserStatsEntity(
                userId = userId,
                hogarId = hogarId,
                tareasCompletadas = 0,
                rachaActual = 0,
                rachaMaxima = 0,
                ultimaActividad = 0L,
                puntuacionTotal = 0,
                gastosCreados = 0,
                tareasCreadas = 0,
                actualizadoEn = System.currentTimeMillis(),
                synced = 0
            )
            userStatsDao.insertUserStats(newStats)
            // Sincronizar con Firestore
            firestoreRepository.syncUserStats(newStats)
        }
    }

    /**
     * Registra la finalización de una tarea
     * @param prioridad: ALTA, MEDIA, BAJA
     * @param completadaAntes: si se completó antes de la fecha límite
     */
    suspend fun onTaskCompleted(
        userId: String,
        hogarId: String,
        prioridad: String = "MEDIA",
        completadaAntes: Boolean = false
    ) {
        initializeUserStats(userId, hogarId)
        
        // Calcular puntos según prioridad
        var puntos = when (prioridad.uppercase()) {
            "ALTA" -> 15
            "BAJA" -> 5
            else -> 10 // MEDIA
        }
        
        // Bonus por completar antes de tiempo
        if (completadaAntes) {
            puntos += 5
        }
        
        val now = System.currentTimeMillis()
        
        // Incrementar contador de tareas y puntos
        userStatsDao.incrementTasksCompleted(userId, hogarId, puntos, now)
        
        // Actualizar racha
        updateStreak(userId, hogarId, now)
        
        // Sincronizar con Firestore
        val updatedStats = userStatsDao.getUserStatsSync(userId, hogarId)
        updatedStats?.let { firestoreRepository.syncUserStats(it) }
    }

    /**
     * Registra la creación de un gasto
     */
    suspend fun onExpenseCreated(userId: String, hogarId: String) {
        initializeUserStats(userId, hogarId)
        
        val puntos = 5
        val now = System.currentTimeMillis()
        
        userStatsDao.incrementExpensesCreated(userId, hogarId, puntos, now)
        
        // Actualizar racha
        updateStreak(userId, hogarId, now)
        
        // Sincronizar con Firestore
        val updatedStats = userStatsDao.getUserStatsSync(userId, hogarId)
        updatedStats?.let { firestoreRepository.syncUserStats(it) }
    }

    /**
     * Registra la creación de una tarea
     */
    suspend fun onTaskCreated(userId: String, hogarId: String) {
        initializeUserStats(userId, hogarId)
        
        val now = System.currentTimeMillis()
        userStatsDao.incrementTasksCreated(userId, hogarId, now)
        
        // Sincronizar con Firestore
        val updatedStats = userStatsDao.getUserStatsSync(userId, hogarId)
        updatedStats?.let { firestoreRepository.syncUserStats(it) }
    }

    /**
     * Actualiza la racha del usuario
     * La racha se mantiene si hay actividad en días consecutivos
     */
    private suspend fun updateStreak(userId: String, hogarId: String, now: Long) {
        val stats = userStatsDao.getUserStatsSync(userId, hogarId) ?: return
        
        val lastActivity = stats.ultimaActividad
        val currentStreak = stats.rachaActual
        
        // Si es la primera actividad
        if (lastActivity == 0L) {
            userStatsDao.updateStreak(userId, hogarId, 1, 2, now)
            return
        }
        
        // Calcular días desde la última actividad
        val daysSinceLastActivity = TimeUnit.MILLISECONDS.toDays(now - lastActivity)
        
        val newStreak: Int
        val bonusPoints: Int
        
        when {
            // Misma fecha (mismo día) - no cambiar racha pero dar puntos
            isSameDay(lastActivity, now) -> {
                newStreak = currentStreak
                bonusPoints = 0 // No dar bonus si es el mismo día
                // Solo actualizar timestamp
                userStatsDao.updateStreak(userId, hogarId, newStreak, bonusPoints, now)
                return
            }
            // Día consecutivo - incrementar racha
            daysSinceLastActivity == 1L -> {
                newStreak = currentStreak + 1
                bonusPoints = 2 // Bonus por mantener la racha
            }
            // Se rompió la racha - reiniciar
            else -> {
                newStreak = 1
                bonusPoints = 0
            }
        }
        
        userStatsDao.updateStreak(userId, hogarId, newStreak, bonusPoints, now)
    }

    /**
     * Verifica si dos timestamps son del mismo día
     */
    private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Obtiene el ranking de usuarios por puntuación
     */
    fun getLeaderboard(hogarId: String): Flow<List<UserStatsEntity>> {
        return userStatsDao.getLeaderboard(hogarId)
    }

    /**
     * Resetea las estadísticas de un usuario (útil para testing o reset manual)
     */
    suspend fun resetUserStats(userId: String, hogarId: String) {
        userStatsDao.deleteUserStats(userId, hogarId)
        initializeUserStats(userId, hogarId)
    }
}
