package com.homeflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.homeflow.app.data.local.entity.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {

    @Query("SELECT * FROM activity_log WHERE hogar_id = :hogarId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentActivity(hogarId: String, limit: Int = 20): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(entry: ActivityLogEntity)

    @Query("DELETE FROM activity_log WHERE hogar_id = :hogarId AND timestamp < :beforeTimestamp")
    suspend fun pruneOldEntries(hogarId: String, beforeTimestamp: Long)
}
