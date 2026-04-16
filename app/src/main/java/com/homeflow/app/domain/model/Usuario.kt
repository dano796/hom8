package com.homeflow.app.domain.model

data class Usuario(
    val id: String,
    val nombre: String,
    val email: String,
    val avatarUrl: String = "",
    val rol: Rol = Rol.MEMBER,
    val hogarId: String = ""
)
