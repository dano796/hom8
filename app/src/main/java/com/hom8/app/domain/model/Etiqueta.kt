package com.hom8.app.domain.model

data class Etiqueta(
    val id: String,
    val nombre: String,
    val color: String = "#888888",
    val hogarId: String
)
