package com.homeflow.app.domain.model

data class Gasto(
    val id: String,
    val descripcion: String,
    val monto: Double,
    val categoria: CategoriaGasto = CategoriaGasto.OTHER,
    val pagadorId: String,
    val hogarId: String,
    val fecha: Long = System.currentTimeMillis(),
    val nota: String = "",
    val participantes: List<ParticipanteGasto> = emptyList()
)

data class ParticipanteGasto(
    val userId: String,
    val montoAsignado: Double = 0.0
)

data class Balance(
    val deOwnedToYou: Double = 0.0,
    val youOwe: Double = 0.0
)

data class Deuda(
    val fromUserId: String,
    val toUserId: String,
    val monto: Double
)

data class Pago(
    val id: String,
    val fromUserId: String,
    val toUserId: String,
    val monto: Double,
    val fecha: Long,
    val nota: String = ""
)
