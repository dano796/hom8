package com.homeflow.app.domain.repository

import com.homeflow.app.domain.model.Tarea
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasksByHome(hogarId: String): Flow<List<Tarea>>
    fun getTasksByUser(userId: String, hogarId: String): Flow<List<Tarea>>
    fun getTasksForDay(hogarId: String, startOfDay: Long, endOfDay: Long): Flow<List<Tarea>>
    fun getUpcomingTasks(hogarId: String, limit: Int = 3): Flow<List<Tarea>>
    fun getOverdueTasks(hogarId: String): Flow<List<Tarea>>
    fun searchTasks(hogarId: String, query: String): Flow<List<Tarea>>
    fun getTaskById(taskId: String): Flow<Tarea?>
    fun getCompletedTaskCount(userId: String, hogarId: String): Flow<Int>

    suspend fun createTask(tarea: Tarea): Result<Tarea>
    suspend fun updateTask(tarea: Tarea): Result<Unit>
    suspend fun deleteTask(taskId: String): Result<Unit>
    suspend fun toggleTaskComplete(taskId: String): Result<Unit>
}
