package com.homeflow.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "labels")
data class LabelEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "color")
    val color: String = "#888888",

    @ColumnInfo(name = "hogar_id")
    val hogarId: String,

    @ColumnInfo(name = "synced")
    val synced: Int = 0
)
