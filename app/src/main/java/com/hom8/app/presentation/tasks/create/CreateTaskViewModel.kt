package com.hom8.app.presentation.tasks.create

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.hom8.app.data.local.dao.ActivityLogDao
import com.hom8.app.data.local.dao.HomeDao
import com.hom8.app.data.local.dao.TaskDao
import com.hom8.app.data.local.dao.UserDao
import com.hom8.app.data.local.entity.ActivityLogEntity
import com.hom8.app.data.local.entity.TaskEntity
import com.hom8.app.data.remote.FirestoreRepository
import com.hom8.app.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class MemberOption(
    val id: String,
    val name: String,
    val initials: String
)

data class CreateTaskUiState(
    val task: TaskEntity? = null,
    val members: List<MemberOption> = emptyList(),
    val selectedAssigneeId: String = "",
    val isLoading: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val homeDao: HomeDao,
    private val userDao: UserDao,
    private val activityLogDao: ActivityLogDao,
    private val session: SessionManager,
    private val firestoreRepo: FirestoreRepository
) : ViewModel() {

    companion object {
        private const val TAG = "CreateTaskViewModel"
    }

    private val _uiState = MutableStateFlow(CreateTaskUiState())
    val uiState: StateFlow<CreateTaskUiState> = _uiState.asStateFlow()

    private val gson = Gson()

    init {
        loadMembers()
    }

    private fun loadMembers() {
        val hogarId = session.hogarId.ifEmpty { return }
        val currentUserId = session.userId
        viewModelScope.launch {
            homeDao.getHomeById(hogarId).collect { home ->
                if (home == null) return@collect
                val memberIds = parseMembersJson(home.miembros)
                val knownUsers = userDao.getUsersByIds(memberIds).associateBy { it.id }
                val members = memberIds.map { uid ->
                    val user = knownUsers[uid]
                    val name = user?.nombre ?: if (uid == currentUserId) session.userName else "Miembro"
                    MemberOption(id = uid, name = name, initials = buildInitials(name))
                }
                val defaultAssignee = if (members.any { it.id == currentUserId }) currentUserId
                                      else members.firstOrNull()?.id ?: currentUserId
                _uiState.update { it.copy(members = members, selectedAssigneeId = defaultAssignee) }
            }
        }
    }

    fun setAssignee(userId: String) {
        _uiState.update { it.copy(selectedAssigneeId = userId) }
    }

    fun loadTask(taskId: String) {
        viewModelScope.launch {
            taskDao.getTaskById(taskId).collect { task ->
                _uiState.update { state ->
                    state.copy(
                        task = task,
                        selectedAssigneeId = task?.responsableId?.takeIf { it.isNotEmpty() }
                            ?: state.selectedAssigneeId
                    )
                }
            }
        }
    }

    fun saveTask(
        taskId: String?,
        titulo: String,
        descripcion: String,
        prioridad: String,
        fechaLimite: Long?,
        checklistItems: List<String>
    ) {
        viewModelScope.launch {
            try {
                val checklistJson = gson.toJson(checklistItems.map { mapOf("texto" to it, "completado" to false) })
                val now = System.currentTimeMillis()
                val hogarId = session.hogarId.ifEmpty { "default" }
                val userId = session.userId.ifEmpty { "default" }
                val assigneeId = _uiState.value.selectedAssigneeId.ifEmpty { userId }

                val entity = TaskEntity(
                    id = taskId ?: UUID.randomUUID().toString(),
                    titulo = titulo,
                    descripcion = descripcion,
                    responsableId = assigneeId,
                    creadoPor = if (taskId != null) _uiState.value.task?.creadoPor ?: userId else userId,
                    hogarId = hogarId,
                    fechaLimite = fechaLimite,
                    prioridad = prioridad,
                    estado = if (taskId != null) {
                        _uiState.value.task?.estado ?: "PENDIENTE"
                    } else "PENDIENTE",
                    checklist = checklistJson,
                    creadoEn = if (taskId != null) _uiState.value.task?.creadoEn ?: now else now,
                    actualizadoEn = now,
                    synced = 0
                )

                taskDao.insertTask(entity)
                
                // Obtener nombres de usuarios para información adicional
                val creatorName = if (entity.creadoPor == userId) {
                    session.userName
                } else {
                    userDao.getUsersByIds(listOf(entity.creadoPor)).firstOrNull()?.nombre ?: "Usuario"
                }
                
                val assigneeName = if (assigneeId == userId) {
                    session.userName
                } else {
                    userDao.getUsersByIds(listOf(assigneeId)).firstOrNull()?.nombre ?: "Usuario"
                }
                
                firestoreRepo.syncTaskWithMetadata(entity, creatorName, assigneeName)

                val tipo = if (taskId != null) "TASK_UPDATED" else "TASK_CREATED"
                val log = ActivityLogEntity(
                    id = UUID.randomUUID().toString(),
                    hogarId = hogarId,
                    actorId = userId,
                    actorName = session.userName.ifEmpty { "Alguien" },
                    tipo = tipo,
                    targetTitle = titulo,
                    timestamp = now
                )
                
                activityLogDao.insertActivity(log)
                firestoreRepo.syncActivityLog(log)

                _uiState.update { it.copy(saved = true) }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to save task: ${e.message}", e)
                _uiState.update { it.copy(error = e.message ?: "Error al guardar la tarea") }
            }
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun parseMembersJson(json: String): List<String> {
        val trimmed = json.trim().removePrefix("[").removeSuffix("]")
        if (trimmed.isBlank()) return emptyList()
        return trimmed.split(",").map { it.trim().removeSurrounding("\"") }.filter { it.isNotEmpty() }
    }

    private fun buildInitials(name: String): String =
        name.trim().split(" ")
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2).joinToString("").ifEmpty { "?" }
}
