package com.homeflow.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persistent activity log. One row per action (task or expense created/updated/completed/deleted).
 * Read by the Dashboard to populate the "Recent Activity" feed.
 */
@Entity(tableName = "activity_log")
data class ActivityLogEntity(

    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "hogar_id")
    val hogarId: String,

    @ColumnInfo(name = "actor_id")
    val actorId: String,

    @ColumnInfo(name = "actor_name")
    val actorName: String,

    /**
     * Type of event. One of:
     * TASK_CREATED, TASK_UPDATED, TASK_COMPLETED, TASK_UNCOMPLETED, TASK_DELETED,
     * EXPENSE_CREATED, EXPENSE_UPDATED, EXPENSE_DELETED
     */
    @ColumnInfo(name = "tipo")
    val tipo: String,

    /** Human-readable title/description of the affected item */
    @ColumnInfo(name = "target_title")
    val targetTitle: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
