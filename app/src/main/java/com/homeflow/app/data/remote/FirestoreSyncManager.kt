package com.homeflow.app.data.remote

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.homeflow.app.data.local.dao.ActivityLogDao
import com.homeflow.app.data.local.dao.ExpenseDao
import com.homeflow.app.data.local.dao.HomeDao
import com.homeflow.app.data.local.dao.TaskDao
import com.homeflow.app.data.local.entity.ActivityLogEntity
import com.homeflow.app.data.local.entity.ExpenseEntity
import com.homeflow.app.data.local.entity.HomeEntity
import com.homeflow.app.data.local.entity.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Subscribes to Firestore real-time listeners and keeps Room in sync.
 * Started when the user enters the main screen, stopped on sign-out.
 */
@Singleton
class FirestoreSyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val taskDao: TaskDao,
    private val expenseDao: ExpenseDao,
    private val activityLogDao: ActivityLogDao,
    private val homeDao: HomeDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val listeners = mutableListOf<ListenerRegistration>()

    fun startSync(hogarId: String) {
        stopSync()
        listenToTasks(hogarId)
        listenToExpenses(hogarId)
        listenToActivityLog(hogarId)
        listenToHome(hogarId)
    }

    fun stopSync() {
        listeners.forEach { it.remove() }
        listeners.clear()
    }

    // ─── Tasks ────────────────────────────────────────────────────────────────

    private fun listenToTasks(hogarId: String) {
        val reg = firestore.collection("homes/$hogarId/tasks")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch {
                    snapshot.documentChanges.forEach { change ->
                        when (change.type) {
                            DocumentChange.Type.ADDED,
                            DocumentChange.Type.MODIFIED -> {
                                change.document.toTask(hogarId)?.let { taskDao.insertTask(it) }
                            }
                            DocumentChange.Type.REMOVED -> {
                                taskDao.deleteTaskById(change.document.id)
                            }
                        }
                    }
                }
            }
        listeners.add(reg)
    }

    // ─── Expenses ─────────────────────────────────────────────────────────────

    private fun listenToExpenses(hogarId: String) {
        val reg = firestore.collection("homes/$hogarId/expenses")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch {
                    snapshot.documentChanges.forEach { change ->
                        when (change.type) {
                            DocumentChange.Type.ADDED,
                            DocumentChange.Type.MODIFIED -> {
                                change.document.toExpense(hogarId)?.let { expenseDao.insertExpense(it) }
                            }
                            DocumentChange.Type.REMOVED -> {
                                expenseDao.deleteExpenseById(change.document.id)
                            }
                        }
                    }
                }
            }
        listeners.add(reg)
    }

    // ─── Activity log ─────────────────────────────────────────────────────────

    private fun listenToActivityLog(hogarId: String) {
        val reg = firestore.collection("homes/$hogarId/activity_log")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch {
                    snapshot.documentChanges.forEach { change ->
                        if (change.type == DocumentChange.Type.ADDED ||
                            change.type == DocumentChange.Type.MODIFIED
                        ) {
                            change.document.toActivityLog(hogarId)?.let {
                                activityLogDao.insertActivity(it)
                            }
                        }
                    }
                }
            }
        listeners.add(reg)
    }

    // ─── Home ─────────────────────────────────────────────────────────────────

    private fun listenToHome(hogarId: String) {
        val reg = firestore.collection("homes")
            .document(hogarId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                scope.launch {
                    snapshot.toHome()?.let { homeDao.insertHome(it) }
                }
            }
        listeners.add(reg)
    }

    // ─── Firestore document → Entity ─────────────────────────────────────────

    private fun com.google.firebase.firestore.DocumentSnapshot.toTask(hogarId: String): TaskEntity? {
        return try {
            TaskEntity(
                id = getString("id") ?: id,
                titulo = getString("titulo") ?: return null,
                descripcion = getString("descripcion") ?: "",
                responsableId = getString("responsableId") ?: "",
                creadoPor = getString("creadoPor") ?: "",
                hogarId = getString("hogarId") ?: hogarId,
                fechaLimite = getLong("fechaLimite"),
                prioridad = getString("prioridad") ?: "MEDIUM",
                estado = getString("estado") ?: "PENDING",
                recurrencia = getString("recurrencia"),
                etiquetas = getString("etiquetas") ?: "[]",
                checklist = getString("checklist") ?: "[]",
                comentarios = getString("comentarios") ?: "[]",
                actividad = getString("actividad") ?: "[]",
                adjuntos = getString("adjuntos") ?: "[]",
                creadoEn = getLong("creadoEn") ?: System.currentTimeMillis(),
                actualizadoEn = getLong("actualizadoEn") ?: System.currentTimeMillis(),
                synced = 1
            )
        } catch (e: Exception) { null }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toExpense(hogarId: String): ExpenseEntity? {
        return try {
            ExpenseEntity(
                id = getString("id") ?: id,
                descripcion = getString("descripcion") ?: return null,
                monto = getDouble("monto") ?: return null,
                categoria = getString("categoria") ?: "OTHER",
                pagadorId = getString("pagadorId") ?: "",
                hogarId = getString("hogarId") ?: hogarId,
                fecha = getLong("fecha") ?: System.currentTimeMillis(),
                nota = getString("nota") ?: "",
                participantes = getString("participantes") ?: "[]",
                synced = 1
            )
        } catch (e: Exception) { null }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toActivityLog(hogarId: String): ActivityLogEntity? {
        return try {
            ActivityLogEntity(
                id = getString("id") ?: id,
                hogarId = getString("hogarId") ?: hogarId,
                actorId = getString("actorId") ?: "",
                actorName = getString("actorName") ?: "",
                tipo = getString("tipo") ?: "",
                targetTitle = getString("targetTitle") ?: "",
                timestamp = getLong("timestamp") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) { null }
    }

    @Suppress("UNCHECKED_CAST")
    private fun com.google.firebase.firestore.DocumentSnapshot.toHome(): HomeEntity? {
        return try {
            val memberIds = get("memberIds") as? List<String> ?: emptyList()
            val miembros = "[" + memberIds.joinToString(",") { "\"$it\"" } + "]"
            HomeEntity(
                id = getString("id") ?: id,
                nombre = getString("nombre") ?: return null,
                codigoInvitacion = getString("codigoInvitacion") ?: "",
                miembros = miembros,
                gastosModo = getString("gastosModo") ?: "SPLIT",
                creadoEn = getLong("creadoEn") ?: System.currentTimeMillis(),
                synced = 1
            )
        } catch (e: Exception) { null }
    }
}
