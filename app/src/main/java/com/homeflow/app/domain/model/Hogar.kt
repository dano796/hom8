package com.homeflow.app.domain.model

data class Hogar(
    val id: String,
    val nombre: String,
    val codigoInvitacion: String = "",
    val miembros: List<String> = emptyList(),
    val creadoEn: Long = System.currentTimeMillis()
)
