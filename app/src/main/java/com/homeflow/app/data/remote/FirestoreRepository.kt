package com.homeflow.app.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.homeflow.app.data.local.entity.ActivityLogEntity
import com.homeflow.app.data.local.entity.ExpenseEntity
import com.homeflow.app.data.local.entity.HomeEntity
import com.homeflow.app.data.local.entity.TaskEntity
import com.homeflow.app.data.local.entity.UserEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Writes local entity changes to Firestore.
 * All methods are fire-and-forget — Firestore queues writes offline automatically.
 */
@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    // ─── Tasks ────────────────────────────────────────────────────────────────

    fun syncTask(task: TaskEntity) {
        firestore.collection("homes/${task.hogarId}/tasks")
            .document(task.id)
            .set(task.toMap())
    }

    fun deleteTask(hogarId: String, taskId: String) {
        firestore.collection("homes/$hogarId/tasks")
            .document(taskId)
            .delete()
    }

    // ─── Expenses ─────────────────────────────────────────────────────────────

    fun syncExpense(expense: ExpenseEntity) {
        firestore.collection("homes/${expense.hogarId}/expenses")
            .document(expense.id)
            .set(expense.toMap())
    }

    fun deleteExpense(hogarId: String, expenseId: String) {
        firestore.collection("homes/$hogarId/expenses")
            .document(expenseId)
            .delete()
    }

    // ─── Homes ────────────────────────────────────────────────────────────────

    fun syncHome(home: HomeEntity) {
        firestore.collection("homes")
            .document(home.id)
            .set(home.toMap())
    }

    // ─── Activity log ─────────────────────────────────────────────────────────

    fun syncActivityLog(log: ActivityLogEntity) {
        firestore.collection("homes/${log.hogarId}/activity_log")
            .document(log.id)
            .set(log.toMap())
    }

    // ─── Users ────────────────────────────────────────────────────────────────

    fun syncUser(user: UserEntity) {
        firestore.collection("users")
            .document(user.id)
            .set(user.toMap())
    }

    // ─── Entity → Firestore map ───────────────────────────────────────────────

    private fun TaskEntity.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "titulo" to titulo,
        "descripcion" to descripcion,
        "responsableId" to responsableId,
        "creadoPor" to creadoPor,
        "hogarId" to hogarId,
        "fechaLimite" to fechaLimite,
        "prioridad" to prioridad,
        "estado" to estado,
        "recurrencia" to recurrencia,
        "etiquetas" to etiquetas,
        "checklist" to checklist,
        "comentarios" to comentarios,
        "actividad" to actividad,
        "adjuntos" to adjuntos,
        "creadoEn" to creadoEn,
        "actualizadoEn" to actualizadoEn
    )

    private fun ExpenseEntity.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "descripcion" to descripcion,
        "monto" to monto,
        "categoria" to categoria,
        "pagadorId" to pagadorId,
        "hogarId" to hogarId,
        "fecha" to fecha,
        "nota" to nota,
        "participantes" to participantes
    )

    private fun HomeEntity.toMap(): Map<String, Any?> {
        val memberIds = parseMembersJson(miembros)
        return mapOf(
            "id" to id,
            "nombre" to nombre,
            "codigoInvitacion" to codigoInvitacion,
            "memberIds" to memberIds,
            "gastosModo" to gastosModo,
            "creadoEn" to creadoEn
        )
    }

    private fun ActivityLogEntity.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "hogarId" to hogarId,
        "actorId" to actorId,
        "actorName" to actorName,
        "tipo" to tipo,
        "targetTitle" to targetTitle,
        "timestamp" to timestamp
    )

    private fun UserEntity.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "nombre" to nombre,
        "email" to email,
        "avatarUrl" to avatarUrl
    )

    private fun parseMembersJson(json: String): List<String> {
        val trimmed = json.trim().removePrefix("[").removeSuffix("]")
        if (trimmed.isBlank()) return emptyList()
        return trimmed.split(",").map { it.trim().removeSurrounding("\"") }.filter { it.isNotEmpty() }
    }
}
