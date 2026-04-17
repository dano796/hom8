package com.hom8.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hom8.app.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE hogar_id = :hogarId ORDER BY creado_en DESC")
    fun getTasksByHome(hogarId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE responsable_id = :userId AND hogar_id = :hogarId ORDER BY fecha_limite ASC")
    fun getTasksByUser(userId: String, hogarId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE hogar_id = :hogarId AND fecha_limite BETWEEN :startOfDay AND :endOfDay ORDER BY fecha_limite ASC")
    fun getTasksForDay(hogarId: String, startOfDay: Long, endOfDay: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE hogar_id = :hogarId AND fecha_limite BETWEEN :startDate AND :endDate ORDER BY fecha_limite ASC")
    fun getTasksByDateRange(hogarId: String, startDate: Long, endDate: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE hogar_id = :hogarId AND estado != 'DONE' AND fecha_limite < :now ORDER BY fecha_limite ASC")
    fun getOverdueTasks(hogarId: String, now: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE hogar_id = :hogarId AND (titulo LIKE '%' || :query || '%' OR etiquetas LIKE '%' || :query || '%') ORDER BY creado_en DESC")
    fun searchTasks(hogarId: String, query: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskById(taskId: String): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE hogar_id = :hogarId AND estado != 'DONE' ORDER BY fecha_limite ASC LIMIT :limit")
    fun getUpcomingTasks(hogarId: String, limit: Int = 3): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    @Query("SELECT COUNT(*) FROM tasks WHERE responsable_id = :userId AND hogar_id = :hogarId AND estado = 'DONE'")
    fun getCompletedTaskCount(userId: String, hogarId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE hogar_id = :hogarId")
    fun getTotalTaskCount(hogarId: String): Flow<Int>
}
