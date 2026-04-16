package com.homeflow.app.presentation.tasks.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.homeflow.app.data.local.dao.ActivityLogDao
import com.homeflow.app.data.local.dao.HomeDao
import com.homeflow.app.data.local.dao.TaskDao
import com.homeflow.app.data.local.dao.UserDao
import com.homeflow.app.data.local.entity.ActivityLogEntity
import com.homeflow.app.data.local.entity.TaskEntity
import com.homeflow.app.data.remote.FirestoreRepository
import com.homeflow.app.util.SessionManager
import java.util.UUID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskDetailUiState(
    val task: TaskEntity? = null,
    val createdByName: String = "",
    val assigneeName: String = "Unassigned",
    val isLoading: Boolean = true,
    val deleted: Boolean = false
)

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val homeDao: HomeDao,
    private val userDao: UserDao,
    private val activityLogDao: ActivityLogDao,
    private val session: SessionManager,
    private val firestoreRepo: FirestoreRepository
) : ViewModel() {

    /** userId → display name, populated once the home loads */
    private var membersMap: Map<String, String> = emptyMap()

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    private val gson = Gson()

    init {
        loadMembers()
    }

    private fun loadMembers() {
        val hogarId = session.hogarId.ifEmpty { return }
        viewModelScope.launch {
            homeDao.getHomeById(hogarId).collect { home ->
                if (home == null) return@collect
                val memberIds = parseMembersJson(home.miembros)
                val users = userDao.getUsersByIds(memberIds)
                membersMap = users.associate { it.id to it.nombre } +
                    mapOf(session.userId to session.userName.ifEmpty { "You" })
                // Re-resolve names on the current task if already loaded
                _uiState.value.task?.let { resolveNames(it) }
            }
        }
    }

    fun loadTask(taskId: String) {
        viewModelScope.launch {
            taskDao.getTaskById(taskId).collect { task ->
                if (task == null) {
                    _uiState.update { it.copy(isLoading = false) }
                } else {
                    resolveNames(task)
                }
            }
        }
    }

    private fun resolveNames(task: TaskEntity) {
        val currentUserId = session.userId
        val assigneeName = when {
            task.responsableId.isEmpty() -> "Unassigned"
            task.responsableId == currentUserId ->
                session.userName.ifEmpty { "You" }
            else ->
                membersMap[task.responsableId] ?: task.responsableId.take(8)
        }
        val createdByName = when {
            task.creadoPor.isEmpty() -> ""
            task.creadoPor == currentUserId ->
                session.userName.ifEmpty { "You" }
            else ->
                membersMap[task.creadoPor] ?: task.creadoPor.take(8)
        }
        _uiState.update { it.copy(task = task, assigneeName = assigneeName, createdByName = createdByName, isLoading = false) }
    }

    fun toggleComplete() {
        val task = _uiState.value.task ?: return
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

    fun toggleChecklistItem(index: Int, checked: Boolean) {
        val task = _uiState.value.task ?: return
        viewModelScope.launch {
            val type = object : TypeToken<MutableList<MutableMap<String, Any>>>() {}.type
            val items: MutableList<MutableMap<String, Any>> = try {
                gson.fromJson(task.checklist, type) ?: mutableListOf()
            } catch (e: Exception) { mutableListOf() }

            if (index < items.size) {
                items[index]["completado"] = checked
                val updatedChecklist = gson.toJson(items)
                val updated = task.copy(checklist = updatedChecklist, actualizadoEn = System.currentTimeMillis())
                taskDao.updateTask(updated)
                firestoreRepo.syncTask(updated)
            }
        }
    }

    fun deleteTask() {
        val task = _uiState.value.task ?: return
        viewModelScope.launch {
            taskDao.deleteTask(task)
            firestoreRepo.deleteTask(task.hogarId, task.id)
            val log = ActivityLogEntity(
                id = UUID.randomUUID().toString(),
                hogarId = task.hogarId,
                actorId = session.userId,
                actorName = session.userName.ifEmpty { "Someone" },
                tipo = "TASK_DELETED",
                targetTitle = task.titulo,
                timestamp = System.currentTimeMillis()
            )
            activityLogDao.insertActivity(log)
            firestoreRepo.syncActivityLog(log)
            _uiState.update { it.copy(deleted = true) }
        }
    }

    private fun parseMembersJson(json: String): List<String> {
        val trimmed = json.trim().removePrefix("[").removeSuffix("]")
        if (trimmed.isBlank()) return emptyList()
        return trimmed.split(",").map { it.trim().removeSurrounding("\"") }.filter { it.isNotEmpty() }
    }
}
