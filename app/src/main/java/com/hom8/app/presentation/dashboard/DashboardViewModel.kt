package com.hom8.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hom8.app.R
import com.hom8.app.data.local.dao.ActivityLogDao
import com.hom8.app.data.local.dao.ExpenseDao
import com.hom8.app.data.local.dao.HomeDao
import com.hom8.app.data.local.dao.NotificationDao
import com.hom8.app.data.local.dao.TaskDao
import com.hom8.app.data.local.entity.ActivityLogEntity
import com.hom8.app.data.local.entity.TaskEntity
import com.hom8.app.data.remote.FirestoreRepository
import com.hom8.app.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val expenseDao: ExpenseDao,
    private val notificationDao: NotificationDao,
    private val homeDao: HomeDao,
    private val activityLogDao: ActivityLogDao,
    private val session: SessionManager,
    private val firestoreRepo: FirestoreRepository,
    private val userStatsRepository: com.hom8.app.domain.repository.UserStatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        val hogarId = session.hogarId.ifEmpty { "default" }
        val userId = session.userId.ifEmpty { "default" }

        _uiState.update { it.copy(
            greeting = buildGreeting(),
            userName = session.userName,
            userInitials = session.userInitials.ifEmpty { "U" }
        )}

        val todayRange = getTodayRange()
        val monthRange = getMonthRange()

        viewModelScope.launch {
            combine(
                taskDao.getTasksForDay(hogarId, todayRange.first, todayRange.second),
                taskDao.getUpcomingTasks(hogarId, 5),
                expenseDao.getExpensesByDateRange(hogarId, monthRange.first, monthRange.second),
                combine(
                    notificationDao.getUnreadCount(),
                    activityLogDao.getRecentActivity(hogarId, 20),
                    homeDao.getHomeById(hogarId)
                ) { unread, log, home -> Triple(unread, log, home) }
            ) { todayTasks: List<TaskEntity>,
                upcomingTasks: List<TaskEntity>,
                expenses,
                (unread, activityLog, home) ->

                val isSplitMode = home?.gastosModo == "SPLIT"
                val monthTotal = expenses.sumOf { it.monto }
                val partnerExpenses = expenses.filter { it.pagadorId != userId }.sumOf { it.monto }
                val youOwe = (partnerExpenses / 2).coerceAtLeast(0.0)
                val oweLabel = if (session.partnerName.isNotEmpty())
                    "→ ${session.partnerName}" else "→ Compañero"

                DashboardUiState(
                    greeting = buildGreeting(),
                    userName = session.userName,
                    userInitials = session.userInitials.ifEmpty { "U" },
                    todayTasks = todayTasks,
                    upcomingTasks = upcomingTasks,
                    activityFeed = mapActivityFeed(activityLog),
                    monthTotal = monthTotal,
                    youOweAmount = youOwe,
                    oweLabel = oweLabel,
                    unreadNotifications = unread,
                    isSplitMode = isSplitMode
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun toggleTaskDone(task: TaskEntity): Boolean {
        // Verificar que el usuario actual sea el responsable de la tarea
        val currentUserId = session.userId
        if (task.responsableId != currentUserId) {
            android.util.Log.w("DashboardViewModel", "⚠️ Usuario $currentUserId intentó completar tarea asignada a ${task.responsableId}")
            return false
        }
        
        viewModelScope.launch {
            val newStatus = if (task.estado == "TERMINADO") "PENDIENTE" else "TERMINADO"
            val now = System.currentTimeMillis()
            
            // Determinar si se deben otorgar puntos
            val shouldAwardPoints = newStatus == "TERMINADO" && !task.puntosOtorgados
            
            val updated = task.copy(
                estado = newStatus, 
                actualizadoEn = now,
                puntosOtorgados = if (shouldAwardPoints) true else task.puntosOtorgados
            )
            
            taskDao.updateTask(updated)
            firestoreRepo.syncTask(updated)
            
            val tipo = if (newStatus == "TERMINADO") "TASK_COMPLETED" else "TASK_UNCOMPLETED"
            val log = ActivityLogEntity(
                id = UUID.randomUUID().toString(),
                hogarId = task.hogarId,
                actorId = session.userId,
                actorName = session.userName.ifEmpty { "Alguien" },
                tipo = tipo,
                targetTitle = task.titulo,
                timestamp = now
            )
            activityLogDao.insertActivity(log)
            firestoreRepo.syncActivityLog(log)
            
            // Actualizar estadísticas solo la primera vez que se completa la tarea
            if (shouldAwardPoints) {
                // Determinar si se completó antes de tiempo
                val completadaAntes = task.fechaLimite?.let { it > now } ?: false
                
                // Registrar la tarea completada para el usuario responsable
                userStatsRepository.onTaskCompleted(
                    userId = task.responsableId,
                    hogarId = task.hogarId,
                    prioridad = task.prioridad,
                    completadaAntes = completadaAntes
                )
            }
        }
        return true
    }

    private fun buildGreeting(): String {
        val bogota = TimeZone.getTimeZone("America/Bogota")
        val hour = Calendar.getInstance(bogota).get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Buenos días,"
            hour < 18 -> "Buenas tardes,"
            else      -> "Buenas noches,"
        }
    }

    private fun mapActivityFeed(log: List<ActivityLogEntity>): List<ActivityItem> {
        return log.mapIndexed { index, entry ->
            val initials = entry.actorName
                .trim().split(" ")
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .take(2).joinToString("").ifEmpty { "?" }

            val text = when (entry.tipo) {
                "TASK_CREATED"     -> "${entry.actorName} creó la tarea \"${entry.targetTitle}\""
                "TASK_UPDATED"     -> "${entry.actorName} actualizó la tarea \"${entry.targetTitle}\""
                "TASK_COMPLETED"   -> "${entry.actorName} completó la tarea \"${entry.targetTitle}\""
                "TASK_UNCOMPLETED" -> "${entry.actorName} reabrió \"${entry.targetTitle}\""
                "TASK_DELETED"     -> "${entry.actorName} eliminó la tarea \"${entry.targetTitle}\""
                "EXPENSE_CREATED"  -> "${entry.actorName} añadió el gasto \"${entry.targetTitle}\""
                "EXPENSE_UPDATED"  -> "${entry.actorName} actualizó el gasto \"${entry.targetTitle}\""
                "EXPENSE_DELETED"  -> "${entry.actorName} eliminó el gasto \"${entry.targetTitle}\""
                else               -> "${entry.actorName} hizo algo con \"${entry.targetTitle}\""
            }

            val dotColor = when (entry.tipo) {
                "TASK_COMPLETED"   -> R.color.colorTertiary
                "TASK_DELETED",
                "EXPENSE_DELETED"  -> R.color.colorError
                "EXPENSE_CREATED",
                "EXPENSE_UPDATED"  -> R.color.colorSecondary
                else               -> R.color.colorPrimary
            }

            ActivityItem(
                actorInitials = initials,
                actorColorIndex = index % 6,
                text = text,
                timeAgo = formatTimeAgo(entry.timestamp),
                dotColor = dotColor
            )
        }
    }

    private fun formatTimeAgo(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val minutes = diff / 60_000
        val hours = minutes / 60
        val days = hours / 24
        return when {
            minutes < 1 -> "justo ahora"
            minutes < 60 -> "${minutes}m"
            hours < 24 -> "${hours}h"
            days == 1L -> "Ayer"
            else -> "${days}d"
        }
    }

    private fun getTodayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val end = cal.timeInMillis
        return Pair(start, end)
    }

    private fun getMonthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val end = cal.timeInMillis
        return Pair(start, end)
    }
}
