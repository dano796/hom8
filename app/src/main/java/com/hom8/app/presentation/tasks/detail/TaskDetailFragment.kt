package com.hom8.app.presentation.tasks.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hom8.app.R
import com.hom8.app.data.local.entity.TaskEntity
import com.hom8.app.presentation.common.WireAvatar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class TaskDetailFragment : Fragment() {

    private val viewModel: TaskDetailViewModel by viewModels()
    private val args: TaskDetailFragmentArgs by navArgs()
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_task_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageButton>(R.id.btnDetailBack).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<ImageButton>(R.id.btnDetailMenu).setOnClickListener { anchor ->
            showDetailMenu(anchor)
        }

        view.findViewById<MaterialButton>(R.id.btnMarkComplete).setOnClickListener {
            val state = viewModel.uiState.value
            if (state.canToggleComplete) {
                viewModel.toggleComplete()
            } else {
                // Mostrar mensaje explicando por qué no puede completar la tarea
                state.toggleDisabledReason?.let { reason ->
                    Snackbar.make(requireView(), reason, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        viewModel.loadTask(args.taskId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    state.task?.let { bindTask(view, it, state.assigneeName, state.createdByName) }
                    if (state.deleted) {
                        Snackbar.make(requireView(), R.string.tasks_deleted_message, Snackbar.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    private fun bindTask(view: View, task: TaskEntity, assigneeName: String, createdByName: String) {
        val ctx = requireContext()

        view.findViewById<TextView>(R.id.tvDetailTitle).text = task.titulo

        // Priority badge
        val tvPriority = view.findViewById<TextView>(R.id.tvDetailPriority)
        when (task.prioridad) {
            "ALTA" -> {
                tvPriority.text = "ALTA"
                tvPriority.background = ContextCompat.getDrawable(ctx, R.drawable.bg_priority_high)
                tvPriority.setTextColor(ContextCompat.getColor(ctx, R.color.priorityHighText))
            }
            "BAJA" -> {
                tvPriority.text = "BAJA"
                tvPriority.background = ContextCompat.getDrawable(ctx, R.drawable.bg_priority_low)
                tvPriority.setTextColor(ContextCompat.getColor(ctx, R.color.priorityLowText))
            }
            else -> {
                tvPriority.text = "MEDIA"
                tvPriority.background = ContextCompat.getDrawable(ctx, R.drawable.bg_priority_medium)
                tvPriority.setTextColor(ContextCompat.getColor(ctx, R.color.priorityMediumText))
            }
        }

        // Status badge
        val tvStatus = view.findViewById<TextView>(R.id.tvDetailStatus)
        when (task.estado) {
            "TERMINADO" -> {
                tvStatus.text = "TERMINADO"
                tvStatus.background = ContextCompat.getDrawable(ctx, R.drawable.bg_status_done)
                tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.statusDoneText))
            }
            "ATRASADO" -> {
                tvStatus.text = "ATRASADO"
                tvStatus.background = ContextCompat.getDrawable(ctx, R.drawable.bg_status_overdue)
                tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.statusOverdueText))
            }
            else -> {
                tvStatus.text = "PENDIENTE"
                tvStatus.background = ContextCompat.getDrawable(ctx, R.drawable.bg_status_pending)
                tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.statusPendingText))
            }
        }

        // Mark complete button
        val isDone = task.estado == "TERMINADO"
        view.findViewById<MaterialButton>(R.id.btnMarkComplete).apply {
            text = if (isDone) getString(R.string.tasks_mark_pending) else getString(R.string.tasks_mark_complete)
            
            // Habilitar/deshabilitar según si el usuario es el responsable
            val state = viewModel.uiState.value
            isEnabled = state.canToggleComplete
            alpha = if (state.canToggleComplete) 1.0f else 0.5f
            
            // Cambiar el estilo visual cuando está deshabilitado
            if (!state.canToggleComplete) {
                setTextColor(ContextCompat.getColor(ctx, R.color.colorTextSecondary))
            } else {
                setTextColor(ContextCompat.getColor(ctx, R.color.white))
            }
        }

        // Created by row — show only when creadoPor is set
        val rowCreatedBy = view.findViewById<View>(R.id.rowCreatedBy)
        if (createdByName.isNotEmpty()) {
            rowCreatedBy.visibility = View.VISIBLE
            val avatarCreator = view.findViewById<WireAvatar>(R.id.avatarDetailCreator)
            avatarCreator.initials = createdByName.trim().split(" ")
                .mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("").ifEmpty { "?" }
            avatarCreator.setColorForIndex(1)
            view.findViewById<TextView>(R.id.tvDetailCreatedBy).text = createdByName
        } else {
            rowCreatedBy.visibility = View.GONE
        }

        // Assignee
        val avatarAssignee = view.findViewById<WireAvatar>(R.id.avatarDetailAssignee)
        avatarAssignee.initials = assigneeName.trim().split(" ")
            .mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("").ifEmpty { "?" }
        avatarAssignee.setColorForIndex(0)
        view.findViewById<TextView>(R.id.tvDetailAssignee).text = assigneeName

        // Due date
        view.findViewById<TextView>(R.id.tvDetailDueDate).text = task.fechaLimite?.let { ts ->
            SimpleDateFormat("MMM d, yyyy · h:mm a", Locale("es", "ES")).format(Date(ts))
        } ?: "Sin fecha de vencimiento"

        // Description
        val cardDescription = view.findViewById<View>(R.id.cardDescription)
        val tvDescription = view.findViewById<TextView>(R.id.tvDetailDescription)
        if (task.descripcion.isNotBlank()) {
            cardDescription.visibility = View.VISIBLE
            tvDescription.text = task.descripcion
        } else {
            cardDescription.visibility = View.GONE
        }

        // Checklist
        bindChecklist(view, task)
    }

    private fun bindChecklist(view: View, task: TaskEntity) {
        val cardChecklist = view.findViewById<View>(R.id.cardChecklist)
        val layoutChecklistItems = view.findViewById<LinearLayout>(R.id.layoutChecklistItems)
        val progressChecklist = view.findViewById<ProgressBar>(R.id.progressChecklist)
        val tvChecklistProgress = view.findViewById<TextView>(R.id.tvChecklistProgress)

        val items: List<Map<String, Any>> = try {
            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            gson.fromJson(task.checklist, type) ?: emptyList()
        } catch (_: Exception) { emptyList() }

        if (items.isEmpty()) {
            cardChecklist.visibility = View.GONE
            return
        }

        cardChecklist.visibility = View.VISIBLE
        val doneCount = items.count { (it["completado"] as? Boolean) == true }
        val total = items.size
        val pct = if (total > 0) (doneCount * 100) / total else 0

        tvChecklistProgress.text = "$doneCount/$total"
        progressChecklist.progress = pct

        layoutChecklistItems.removeAllViews()
        items.forEachIndexed { index, item ->
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_checklist_display, layoutChecklistItems, false)
            val cb = itemView.findViewById<CheckBox>(R.id.cbChecklistItem)
            val tv = itemView.findViewById<TextView>(R.id.tvChecklistItem)
            val done = (item["completado"] as? Boolean) == true
            cb.isChecked = done
            tv.text = item["texto"] as? String ?: ""
            if (done) {
                tv.alpha = 0.5f
                tv.paintFlags = tv.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                tv.alpha = 1.0f
                tv.paintFlags = tv.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            cb.setOnCheckedChangeListener { _, isChecked ->
                viewModel.toggleChecklistItem(index, isChecked)
            }
            layoutChecklistItems.addView(itemView)
        }
    }

    private fun showDetailMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.task_context_menu, popup.menu)
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menuEditTask -> {
                    val task = viewModel.uiState.value.task ?: return@setOnMenuItemClickListener false
                    val action = TaskDetailFragmentDirections
                        .actionTaskDetailToCreateTask(task.id)
                    findNavController().navigate(action)
                    true
                }
                R.id.menuDeleteTask -> {
                    viewModel.deleteTask()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}
