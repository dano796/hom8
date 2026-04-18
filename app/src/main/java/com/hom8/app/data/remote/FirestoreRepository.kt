package com.hom8.app.data.remote

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hom8.app.data.local.entity.ActivityLogEntity
import com.hom8.app.data.local.entity.ExpenseEntity
import com.hom8.app.data.local.entity.HomeEntity
import com.hom8.app.data.local.entity.TaskEntity
import com.hom8.app.data.local.entity.UserEntity
import com.hom8.app.data.local.entity.UserStatsEntity
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

    companion object {
        private const val TAG = "FirestoreRepository"
    }
    
    private val gson = Gson()

    // ─── Tasks ────────────────────────────────────────────────────────────────

    fun syncTask(task: TaskEntity) {
        val taskData = task.toMap()
        
        firestore.collection("homes/${task.hogarId}/tasks")
            .document(task.id)
            .set(taskData)
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to sync task ${task.id}: ${e.message}", e)
            }
    }
    
    /**
     * Sincroniza una tarea con metadata adicional (nombres de usuarios)
     * para hacer los datos más legibles en Firebase Console
     */
    fun syncTaskWithMetadata(
        task: TaskEntity,
        creatorName: String,
        assigneeName: String
    ) {
        val taskData = task.toMapWithMetadata(creatorName, assigneeName)
        
        firestore.collection("homes/${task.hogarId}/tasks")
            .document(task.id)
            .set(taskData)
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to sync task ${task.id}: ${e.message}", e)
            }
    }

    fun deleteTask(hogarId: String, taskId: String) {
        firestore.collection("homes/$hogarId/tasks")
            .document(taskId)
            .delete()
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to delete task $taskId: ${e.message}", e)
            }
    }

    // ─── Expenses ─────────────────────────────────────────────────────────────

    fun syncExpense(expense: ExpenseEntity) {
        val expenseData = expense.toMap()
        
        firestore.collection("homes/${expense.hogarId}/expenses")
            .document(expense.id)
            .set(expenseData)
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to sync expense ${expense.id}: ${e.message}", e)
            }
    }
    
    /**
     * Sincroniza un gasto con metadata adicional (nombres de usuarios)
     * para hacer los datos más legibles en Firebase Console
     */
    fun syncExpenseWithMetadata(
        expense: ExpenseEntity,
        payerName: String,
        participantNames: Map<String, String>
    ) {
        val expenseData = expense.toMapWithMetadata(payerName, participantNames)
        
        firestore.collection("homes/${expense.hogarId}/expenses")
            .document(expense.id)
            .set(expenseData)
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to sync expense ${expense.id}: ${e.message}", e)
            }
    }

    fun deleteExpense(hogarId: String, expenseId: String) {
        firestore.collection("homes/$hogarId/expenses")
            .document(expenseId)
            .delete()
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to delete expense $expenseId: ${e.message}", e)
            }
    }

    // ─── Payments ─────────────────────────────────────────────────────────────

    fun syncPayment(payment: com.hom8.app.data.local.entity.PaymentEntity) {
        val paymentData = payment.toMap()
        
        firestore.collection("homes/${payment.hogarId}/payments")
            .document(payment.id)
            .set(paymentData)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Payment synced: ${payment.id}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to sync payment ${payment.id}: ${e.message}", e)
            }
    }

    // ─── Homes ────────────────────────────────────────────────────────────────

    fun syncHome(home: HomeEntity) {
        val homeData = home.toMap()
        
        firestore.collection("homes")
            .document(home.id)
            .set(homeData)
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to sync home ${home.id}: ${e.message}", e)
            }
    }

    // ─── Activity log ─────────────────────────────────────────────────────────

    fun syncActivityLog(log: ActivityLogEntity) {
        val logData = log.toMap()
        
        firestore.collection("homes/${log.hogarId}/activity_log")
            .document(log.id)
            .set(logData)
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to sync activity log ${log.id}: ${e.message}", e)
            }
    }

    // ─── Users ────────────────────────────────────────────────────────────────

    fun syncUser(user: UserEntity) {
        val userData = user.toMap()
        
        firestore.collection("users")
            .document(user.id)
            .set(userData)
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to sync user ${user.id}: ${e.message}", e)
            }
    }

    // ─── User Stats ───────────────────────────────────────────────────────────

    fun syncUserStats(stats: UserStatsEntity) {
        val statsData = stats.toMap()
        
        firestore.collection("homes/${stats.hogarId}/user_stats")
            .document(stats.userId)
            .set(statsData)
            .addOnSuccessListener {
                Log.d(TAG, "✅ User stats synced for ${stats.userId}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to sync user stats ${stats.userId}: ${e.message}", e)
            }
    }

    // ─── Entity → Firestore map ───────────────────────────────────────────────

    private fun TaskEntity.toMap(): Map<String, Any?> {
        // Parsear arrays JSON a listas nativas
        val etiquetasList = parseJsonArray(etiquetas)
        val checklistList = parseJsonArray(checklist)
        val comentariosList = parseJsonArray(comentarios)
        val actividadList = parseJsonArray(actividad)
        val adjuntosList = parseJsonArray(adjuntos)
        
        return mapOf(
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
            // Arrays nativos en lugar de strings JSON
            "etiquetas" to etiquetasList,
            "checklist" to checklistList,
            "comentarios" to comentariosList,
            "actividad" to actividadList,
            "adjuntos" to adjuntosList,
            "creadoEn" to creadoEn,
            "actualizadoEn" to actualizadoEn,
            // Timestamp del servidor para sincronización
            "serverTimestamp" to FieldValue.serverTimestamp()
        )
    }
    
    /**
     * Convierte TaskEntity a Map con metadata adicional para mejor legibilidad
     */
    private fun TaskEntity.toMapWithMetadata(
        creatorName: String,
        assigneeName: String
    ): Map<String, Any?> {
        // Parsear arrays JSON a listas nativas
        val etiquetasList = parseJsonArray(etiquetas)
        val checklistList = parseJsonArray(checklist)
        val comentariosList = parseJsonArray(comentarios)
        val actividadList = parseJsonArray(actividad)
        val adjuntosList = parseJsonArray(adjuntos)
        
        return mapOf(
            "id" to id,
            "titulo" to titulo,
            "descripcion" to descripcion,
            // IDs para referencias
            "responsableId" to responsableId,
            "creadoPor" to creadoPor,
            "hogarId" to hogarId,
            // Nombres para legibilidad (metadata)
            "responsableNombre" to assigneeName,
            "creadoPorNombre" to creatorName,
            // Fechas
            "fechaLimite" to fechaLimite,
            "creadoEn" to creadoEn,
            "actualizadoEn" to actualizadoEn,
            // Estados y prioridad
            "prioridad" to prioridad,
            "estado" to estado,
            "recurrencia" to recurrencia,
            // Arrays nativos en lugar de strings JSON
            "etiquetas" to etiquetasList,
            "checklist" to checklistList,
            "comentarios" to comentariosList,
            "actividad" to actividadList,
            "adjuntos" to adjuntosList,
            // Timestamp del servidor para sincronización
            "serverTimestamp" to FieldValue.serverTimestamp()
        )
    }

    private fun ExpenseEntity.toMap(): Map<String, Any?> {
        // Parsear participantes JSON a estructura nativa
        val participantesData = parseParticipantesJson(participantes)
        
        return mapOf(
            "id" to id,
            "descripcion" to descripcion,
            "monto" to monto,
            "categoria" to categoria,
            "pagadorId" to pagadorId,
            "hogarId" to hogarId,
            "fecha" to fecha,
            "nota" to nota,
            // Puede ser lista de strings o lista de maps con montos
            "participantes" to participantesData,
            // Timestamp del servidor
            "serverTimestamp" to FieldValue.serverTimestamp()
        )
    }
    
    /**
     * Convierte ExpenseEntity a Map con metadata adicional para mejor legibilidad
     */
    private fun ExpenseEntity.toMapWithMetadata(
        payerName: String,
        participantNames: Map<String, String>
    ): Map<String, Any?> {
        // Parsear participantes JSON
        val participantesData = parseParticipantesJson(participantes)
        
        // Extraer IDs de participantes según el formato
        val participantIds = when (participantesData) {
            is List<*> -> {
                if (participantesData.isEmpty()) {
                    emptyList()
                } else if (participantesData[0] is String) {
                    // Formato antiguo: lista de IDs
                    participantesData as List<String>
                } else if (participantesData[0] is Map<*, *>) {
                    // Formato nuevo: lista de maps con userId y amount
                    (participantesData as List<Map<String, Any>>).mapNotNull { it["userId"] as? String }
                } else {
                    emptyList()
                }
            }
            else -> emptyList()
        }
        
        // Crear lista de participantes con nombres
        val participantesConNombres = participantIds.map { userId ->
            mapOf(
                "userId" to userId,
                "nombre" to (participantNames[userId] ?: "Usuario")
            )
        }
        
        return mapOf(
            "id" to id,
            "descripcion" to descripcion,
            "monto" to monto,
            "categoria" to categoria,
            // IDs para referencias
            "pagadorId" to pagadorId,
            "hogarId" to hogarId,
            // Nombres para legibilidad
            "pagadorNombre" to payerName,
            // Fechas
            "fecha" to fecha,
            "nota" to nota,
            // Arrays nativos con información completa
            "participantes" to participantesData,  // Formato original (con o sin montos)
            "participantesConNombres" to participantesConNombres,  // Con nombres para legibilidad
            "participantIds" to participantIds,  // IDs simples para queries
            // Timestamp del servidor
            "serverTimestamp" to FieldValue.serverTimestamp()
        )
    }

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

    private fun UserStatsEntity.toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "hogarId" to hogarId,
        "tareasCompletadas" to tareasCompletadas,
        "rachaActual" to rachaActual,
        "rachaMaxima" to rachaMaxima,
        "ultimaActividad" to ultimaActividad,
        "puntuacionTotal" to puntuacionTotal,
        "gastosCreados" to gastosCreados,
        "tareasCreadas" to tareasCreadas,
        "actualizadoEn" to actualizadoEn,
        "serverTimestamp" to FieldValue.serverTimestamp()
    )

    private fun com.hom8.app.data.local.entity.PaymentEntity.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "fromUserId" to fromUserId,
        "toUserId" to toUserId,
        "monto" to monto,
        "fecha" to fecha,
        "nota" to nota,
        "hogarId" to hogarId,
        "serverTimestamp" to FieldValue.serverTimestamp()
    )

    private fun parseMembersJson(json: String): List<String> {
        val trimmed = json.trim().removePrefix("[").removeSuffix("]")
        if (trimmed.isBlank()) return emptyList()
        return trimmed.split(",").map { it.trim().removeSurrounding("\"") }.filter { it.isNotEmpty() }
    }
    
    /**
     * Parsea un string JSON a una lista nativa de Firestore
     * Maneja tanto arrays de strings como arrays de objetos
     */
    private fun parseJsonArray(jsonString: String?): List<Any> {
        if (jsonString.isNullOrBlank() || jsonString == "[]") return emptyList()
        
        return try {
            // Intentar parsear como lista de objetos genéricos
            val type = object : TypeToken<List<Any>>() {}.type
            gson.fromJson<List<Any>>(jsonString, type) ?: emptyList()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse JSON array: $jsonString", e)
            emptyList()
        }
    }
    
    /**
     * Parsea el JSON de participantes a una lista de IDs o estructura con montos
     * Soporta dos formatos:
     * 1. Antiguo: ["userId1", "userId2"]
     * 2. Nuevo: [{"userId": "userId1", "amount": 50000}, {"userId": "userId2", "amount": 30000}]
     */
    private fun parseParticipantesJson(jsonString: String?): Any {
        if (jsonString.isNullOrBlank() || jsonString == "[]") return emptyList<String>()
        
        return try {
            // Intentar parsear como nuevo formato (con montos)
            val typeWithAmounts = object : TypeToken<List<Map<String, Any>>>() {}.type
            val listWithAmounts: List<Map<String, Any>>? = gson.fromJson(jsonString, typeWithAmounts)
            
            if (listWithAmounts != null && listWithAmounts.isNotEmpty() && listWithAmounts[0].containsKey("userId")) {
                // Es el nuevo formato, retornar tal cual
                listWithAmounts
            } else {
                // Intentar parsear como formato antiguo (solo IDs)
                val typeIds = object : TypeToken<List<String>>() {}.type
                gson.fromJson<List<String>>(jsonString, typeIds) ?: emptyList<String>()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse participantes JSON: $jsonString", e)
            emptyList<String>()
        }
    }
}
