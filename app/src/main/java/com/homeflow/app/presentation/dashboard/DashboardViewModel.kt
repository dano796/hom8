package com.homeflow.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeflow.app.R
import com.homeflow.app.data.local.dao.ActivityLogDao
import com.homeflow.app.data.local.dao.ExpenseDao
import com.homeflow.app.data.local.dao.HomeDao
import com.homeflow.app.data.local.dao.NotificationDao
import com.homeflow.app.data.local.dao.TaskDao
import com.homeflow.app.data.local.entity.ActivityLogEntity
import com.homeflow.app.data.local.entity.TaskEntity
import com.homeflow.app.data.remote.FirestoreRepository
import com.homeflow.app.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
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
    private val firestoreRepo: FirestoreRepository
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
                    "→ ${session.partnerName}" else "→ Partner"

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

    fun toggleTaskDone(task: TaskEntity) {
        viewModelScope.launch {
            val newStatus = if (task.estado == "DONE") "PENDING" else "DONE"
            val now = System.currentTimeMillis()
            val updated = task.copy(estado = newStatus, actualizadoEn = now)
            taskDao.updateTask(updated)
            firestoreRepo.syncTask(updated)
            val tipo = if (newStatus == "DONE") "TASK_COMPLETED" else "TASK_UNCOMPLETED"
            val log = ActivityLogEntity(
                id = UUID.randomUUID().toString(),
                hogarId = task.hogarId,
                actorId = session.userId,
                actorName = session.userName.ifEmpty { "Someone" },
                tipo = tipo,
                targetTitle = task.titulo,
                timestamp = now
            )
            activityLogDao.insertActivity(log)
            firestoreRepo.syncActivityLog(log)
        }
    }

    private fun buildGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good morning,"
            hour < 17 -> "Good afternoon,"
            else -> "Good evening,"
        }
    }

    private fun mapActivityFeed(log: List<ActivityLogEntity>): List<ActivityItem> {
        return log.mapIndexed { index, entry ->
            val initials = entry.actorName
                .trim().split(" ")
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .take(2).joinToString("").ifEmpty { "?" }

            val text = when (entry.tipo) {
                "TASK_CREATED"     -> "${entry.actorName} created task \"${entry.targetTitle}\""
                "TASK_UPDATED"     -> "${entry.actorName} updated task \"${entry.targetTitle}\""
                "TASK_COMPLETED"   -> "${entry.actorName} completed \"${entry.targetTitle}\""
                "TASK_UNCOMPLETED" -> "${entry.actorName} reopened \"${entry.targetTitle}\""
                "TASK_DELETED"     -> "${entry.actorName} deleted task \"${entry.targetTitle}\""
                "EXPENSE_CREATED"  -> "${entry.actorName} added expense \"${entry.targetTitle}\""
                "EXPENSE_UPDATED"  -> "${entry.actorName} updated expense \"${entry.targetTitle}\""
                "EXPENSE_DELETED"  -> "${entry.actorName} deleted expense \"${entry.targetTitle}\""
                else               -> "${entry.actorName} did something with \"${entry.targetTitle}\""
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
            minutes < 1 -> "just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days == 1L -> "Yesterday"
            else -> "${days}d ago"
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
