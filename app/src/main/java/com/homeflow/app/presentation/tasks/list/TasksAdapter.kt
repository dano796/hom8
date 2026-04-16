package com.homeflow.app.presentation.tasks.list

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.HorizontalScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.homeflow.app.R
import com.homeflow.app.data.local.entity.TaskEntity
import com.homeflow.app.presentation.common.WireAvatar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TasksAdapter(
    private val onToggleDone: (TaskEntity) -> Unit,
    private val onItemClick: (TaskEntity) -> Unit,
    private val onMenuClick: (TaskEntity, View) -> Unit
) : ListAdapter<TaskEntity, TasksAdapter.ViewHolder>(DIFF) {

    private val gson = Gson()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cbDone: CheckBox = view.findViewById(R.id.cbTaskDone)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvPriority: TextView = view.findViewById(R.id.tvPriority)
        val btnMenu: ImageButton = view.findViewById(R.id.btnTaskMenu)
        val avatarAssignee: WireAvatar = view.findViewById(R.id.avatarAssignee)
        val tvDueDate: TextView = view.findViewById(R.id.tvDueDate)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val scrollLabels: HorizontalScrollView = view.findViewById(R.id.scrollLabels)
        val layoutLabels: LinearLayout = view.findViewById(R.id.layoutLabels)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task_full, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = getItem(position)
        val ctx = holder.itemView.context
        val isDone = task.estado == "DONE"

        // Title with strikethrough when done
        holder.tvTitle.text = task.titulo
        if (isDone) {
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvTitle.alpha = 0.5f
        } else {
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.tvTitle.alpha = 1f
        }

        // Checkbox
        holder.cbDone.isChecked = isDone

        // Priority badge
        when (task.prioridad) {
            "HIGH" -> {
                holder.tvPriority.text = "HIGH"
                holder.tvPriority.background = ContextCompat.getDrawable(ctx, R.drawable.bg_priority_high)
                holder.tvPriority.setTextColor(ContextCompat.getColor(ctx, R.color.priorityHighText))
            }
            "LOW" -> {
                holder.tvPriority.text = "LOW"
                holder.tvPriority.background = ContextCompat.getDrawable(ctx, R.drawable.bg_priority_low)
                holder.tvPriority.setTextColor(ContextCompat.getColor(ctx, R.color.priorityLowText))
            }
            else -> {
                holder.tvPriority.text = "MED"
                holder.tvPriority.background = ContextCompat.getDrawable(ctx, R.drawable.bg_priority_medium)
                holder.tvPriority.setTextColor(ContextCompat.getColor(ctx, R.color.priorityMediumText))
            }
        }

        // Assignee avatar
        holder.avatarAssignee.initials = task.responsableId.take(1).uppercase().ifEmpty { "?" }
        holder.avatarAssignee.setColorForIndex(position % 6)

        // Due date
        if (task.fechaLimite != null) {
            holder.tvDueDate.text = formatDueDate(task.fechaLimite)
            holder.tvDueDate.setTextColor(ContextCompat.getColor(ctx,
                if (isOverdue(task)) R.color.colorError else R.color.colorTextTertiary
            ))
        } else {
            holder.tvDueDate.text = "No due date"
            holder.tvDueDate.setTextColor(ContextCompat.getColor(ctx, R.color.colorTextTertiary))
        }

        // Status chip
        bindStatus(holder.tvStatus, task, ctx)

        // Labels
        val etiquetas = try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(task.etiquetas, type)
        } catch (e: Exception) {
            emptyList()
        }
        if (etiquetas.isNotEmpty()) {
            holder.scrollLabels.visibility = View.VISIBLE
            holder.layoutLabels.removeAllViews()
            etiquetas.take(3).forEach { label ->
                val chip = TextView(ctx).apply {
                    text = label
                    typeface = androidx.core.content.res.ResourcesCompat.getFont(ctx, R.font.jetbrains_mono)
                    textSize = 10f
                    setTextColor(ContextCompat.getColor(ctx, R.color.colorPrimary))
                    setBackgroundResource(R.drawable.bg_label_chip)
                    val px = (8 * resources.displayMetrics.density).toInt()
                    val pyPx = (3 * resources.displayMetrics.density).toInt()
                    setPadding(px, pyPx, px, pyPx)
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.marginEnd = (6 * resources.displayMetrics.density).toInt()
                    layoutParams = params
                }
                holder.layoutLabels.addView(chip)
            }
        } else {
            holder.scrollLabels.visibility = View.GONE
        }

        // Click listeners
        holder.cbDone.setOnClickListener { onToggleDone(task) }
        holder.itemView.setOnClickListener { onItemClick(task) }
        holder.btnMenu.setOnClickListener { onMenuClick(task, holder.btnMenu) }
    }

    private fun bindStatus(tv: TextView, task: TaskEntity, ctx: android.content.Context) {
        when (task.estado) {
            "DONE" -> {
                tv.text = "DONE"
                tv.background = ContextCompat.getDrawable(ctx, R.drawable.bg_status_done)
                tv.setTextColor(ContextCompat.getColor(ctx, R.color.statusDoneText))
            }
            "OVERDUE" -> {
                tv.text = "OVERDUE"
                tv.background = ContextCompat.getDrawable(ctx, R.drawable.bg_status_overdue)
                tv.setTextColor(ContextCompat.getColor(ctx, R.color.statusOverdueText))
            }
            "IN_PROGRESS" -> {
                tv.text = "IN PROGRESS"
                tv.background = ContextCompat.getDrawable(ctx, R.drawable.bg_status_pending)
                tv.setTextColor(ContextCompat.getColor(ctx, R.color.statusInProgressText))
            }
            else -> {
                // Check if actually overdue
                if (task.fechaLimite != null && task.fechaLimite < System.currentTimeMillis() && task.estado != "DONE") {
                    tv.text = "OVERDUE"
                    tv.background = ContextCompat.getDrawable(ctx, R.drawable.bg_status_overdue)
                    tv.setTextColor(ContextCompat.getColor(ctx, R.color.statusOverdueText))
                } else {
                    tv.text = "PENDING"
                    tv.background = ContextCompat.getDrawable(ctx, R.drawable.bg_status_pending)
                    tv.setTextColor(ContextCompat.getColor(ctx, R.color.statusPendingText))
                }
            }
        }
    }

    private fun formatDueDate(timestamp: Long): String {
        val taskDate = Calendar.getInstance().apply { timeInMillis = timestamp }
        val today = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val timeFmt = SimpleDateFormat("h:mm a", Locale.getDefault())
        val time = timeFmt.format(Date(timestamp))
        return when {
            isSameDay(taskDate, today) -> "Today, $time"
            isSameDay(taskDate, tomorrow) -> "Tomorrow, $time"
            else -> SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(timestamp))
        }
    }

    private fun isOverdue(task: TaskEntity): Boolean =
        task.fechaLimite != null &&
                task.fechaLimite < System.currentTimeMillis() &&
                task.estado != "DONE"

    private fun isSameDay(a: Calendar, b: Calendar) =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
                a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<TaskEntity>() {
            override fun areItemsTheSame(old: TaskEntity, new: TaskEntity) = old.id == new.id
            override fun areContentsTheSame(old: TaskEntity, new: TaskEntity) = old == new
        }
    }
}
