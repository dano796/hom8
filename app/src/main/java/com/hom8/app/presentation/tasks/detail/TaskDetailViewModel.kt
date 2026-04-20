package com.hom8.app.presentation.tasks.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hom8.app.data.local.dao.ActivityLogDao
import com.hom8.app.data.local.dao.HomeDao
import com.hom8.app.data.local.dao.TaskDao
import com.hom8.app.data.local.dao.UserDao
import com.hom8.app.data.local.entity.ActivityLogEntity
import com.hom8.app.data.local.entity.TaskEntity
import com.hom8.app.data.remote.FirestoreRepository
import com.hom8.app.domain.repository.UserStatsRepository
import com.hom8.app.util.SessionManager
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
    val assigneeName: String = "Sin asignar",
    val isLoading: Boolean = true,
    val deleted: Boolean = false,
    val canToggleComplete: Boolean = false,
    val toggleDisabledReason: String? = null
)

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val homeDao: HomeDao,
    private val userDao: UserDao,
    private val activityLogDao: ActivityLogDao,
    private val session: SessionManager,
    private val firestoreRepo: FirestoreRepository,
    private val userStatsRepository: UserStatsRepository
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
                    mapOf(session.userId to session.userName.ifEmpty { "Tú" })
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
            task.responsableId.isEmpty() -> "Sin asignar"
            task.responsableId == currentUserId ->
                session.userName.ifEmpty { "Tú" }
            else ->
                membersMap[task.responsableId] ?: task.responsableId.take(8)
        }
        val createdByName = when {
            task.creadoPor.isEmpty() -> ""
            task.creadoPor == currentUserId ->
                session.userName.ifEmpty { "Tú" }
            else ->
                membersMap[task.creadoPor] ?: task.creadoPor.take(8)
        }
        
        // Determinar si el usuario actual puede marcar la tarea como completada
        val canToggleComplete = task.responsableId == currentUserId
        val toggleDisabledReason = if (!canToggleComplete && task.responsableId.isNotEmpty()) {
            "Solo $assigneeName puede marcar esta tarea como completada"
        } else if (task.responsableId.isEmpty()) {
            "Esta tarea no tiene un responsable asignado"
        } else {
            null
        }
        
        _uiState.update { 
            it.copy(
                task = task, 
                assigneeName = assigneeName, 
                createdByName = createdByName, 
                isLoading = false,
                canToggleComplete = canToggleComplete,
                toggleDisabledReason = toggleDisabledReason
            ) 
        }
    }

    fun toggleComplete() {
        val task = _uiState.value.task ?: return
        
        // Verificar que el usuario actual sea el responsable de la tarea
        if (!_uiState.value.canToggleComplete) {
            android.util.Log.w("TaskDetailViewModel", "⚠️ Usuario ${session.userId} intentó completar tarea asignada a ${task.responsableId}")
            return
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
                actorName = session.userName.ifEmpty { "Alguien" },
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
