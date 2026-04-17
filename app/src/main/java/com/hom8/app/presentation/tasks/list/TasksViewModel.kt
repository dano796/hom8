package com.hom8.app.presentation.tasks.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hom8.app.data.local.dao.ActivityLogDao
import com.hom8.app.data.local.dao.TaskDao
import com.hom8.app.data.local.entity.ActivityLogEntity
import com.hom8.app.data.local.entity.TaskEntity
import com.hom8.app.data.remote.FirestoreRepository
import com.hom8.app.util.SessionManager
import java.util.UUID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TasksUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val whoFilter: WhoFilter = WhoFilter.ALL,
    val priorityFilter: PriorityFilter = PriorityFilter.ALL
) {
    val filteredTasks: List<TaskEntity>
        get() {
            var result = tasks
            if (whoFilter == WhoFilter.MINE) {
                // Filtered by userId in the query, shown here for UI state clarity
            }
            result = when (priorityFilter) {
                PriorityFilter.HIGH -> result.filter { it.prioridad == "ALTA" }
                PriorityFilter.MEDIUM -> result.filter { it.prioridad == "MEDIA" }
                PriorityFilter.LOW -> result.filter { it.prioridad == "BAJA" }
                else -> result
            }
            return result
        }
}

enum class WhoFilter { ALL, MINE }
enum class PriorityFilter { ALL, HIGH, MEDIUM, LOW }

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val activityLogDao: ActivityLogDao,
    private val session: SessionManager,
    private val firestoreRepo: FirestoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState(isLoading = true))
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _whoFilter = MutableStateFlow(WhoFilter.ALL)

    init {
        observeTasks()
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeTasks() {
        val hogarId = session.hogarId.ifEmpty { "default" }
        val userId = session.userId.ifEmpty { "default" }

        viewModelScope.launch {
            combine(
                _searchQuery.debounce(300),
                _whoFilter
            ) { query, who -> Pair(query, who) }
                .flatMapLatest { (query, who) ->
                    if (query.isNotBlank()) {
                        taskDao.searchTasks(hogarId, query)
                    } else if (who == WhoFilter.MINE) {
                        taskDao.getTasksByUser(userId, hogarId)
                    } else {
                        taskDao.getTasksByHome(hogarId)
                    }
                }
                .collect { tasks ->
                    _uiState.update { state ->
                        state.copy(
                            tasks = tasks,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setWhoFilter(filter: WhoFilter) {
        _whoFilter.value = filter
        _uiState.update { it.copy(whoFilter = filter) }
    }

    fun setPriorityFilter(filter: PriorityFilter) {
        _uiState.update { it.copy(priorityFilter = filter) }
    }

    fun toggleTaskDone(task: TaskEntity) {
        viewModelScope.launch {
            val newStatus = if (task.estado == "TERMINADO") "PENDIENTE" else "TERMINADO"
            val now = System.currentTimeMillis()
            val updated = task.copy(estado = newStatus, actualizadoEn = now)
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
        }
    }

    fun deleteTask(task: TaskEntity) {
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
        }
    }
}
