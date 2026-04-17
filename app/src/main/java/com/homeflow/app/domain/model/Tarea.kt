package com.homeflow.app.domain.model

data class Tarea(
    val id: String,
    val titulo: String,
    val descripcion: String = "",
    val responsableId: String = "",
    val hogarId: String = "",
    val fechaLimite: Long? = null,
    val prioridad: Prioridad = Prioridad.MEDIA,
    val estado: EstadoTarea = EstadoTarea.PENDIENTE,
    val recurrencia: Recurrencia? = null,
    val etiquetas: List<String> = emptyList(),
    val checklist: List<ChecklistItem> = emptyList(),
    val comentarios: List<Comentario> = emptyList(),
    val actividad: List<ActividadItem> = emptyList(),
    val adjuntos: List<Adjunto> = emptyList(),
    val creadoEn: Long = System.currentTimeMillis(),
    val actualizadoEn: Long = System.currentTimeMillis()
)

data class Recurrencia(
    val frecuencia: Frecuencia,
    val diasRepeticion: List<Int> = emptyList(),
    val fechaFin: Long? = null
)

data class ChecklistItem(
    val texto: String,
    val completado: Boolean = false
)

data class Comentario(
    val autorId: String,
    val texto: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ActividadItem(
    val tipo: String,
    val descripcion: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class Adjunto(
    val nombre: String,
    val url: String = "",
    val tipo: String = ""
)
