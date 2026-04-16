package com.homeflow.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String = "",

    @ColumnInfo(name = "rol")
    val rol: String = "MEMBER",

    @ColumnInfo(name = "hogar_id")
    val hogarId: String = "",

    @ColumnInfo(name = "synced")
    val synced: Int = 0
)
