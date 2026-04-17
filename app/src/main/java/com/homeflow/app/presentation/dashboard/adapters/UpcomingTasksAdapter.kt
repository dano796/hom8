package com.homeflow.app.presentation.dashboard.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.homeflow.app.R
import com.homeflow.app.data.local.entity.TaskEntity
import com.homeflow.app.presentation.common.WireAvatar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UpcomingTasksAdapter(
    val userInitials: String,
    private val onItemClick: (TaskEntity) -> Unit
) : ListAdapter<TaskEntity, UpcomingTasksAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val viewBar: View = view.findViewById(R.id.viewPriorityBar)
        val tvTitle: TextView = view.findViewById(R.id.tvUpcomingTitle)
        val tvDate: TextView = view.findViewById(R.id.tvUpcomingDate)
        val avatarAssignee: WireAvatar = view.findViewById(R.id.avatarAssignee)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task_upcoming, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = getItem(position)
        val ctx = holder.itemView.context

        holder.tvTitle.text = task.titulo

        // Date label
        holder.tvDate.text = task.fechaLimite?.let { formatDate(it) } ?: "Sin fecha"

        // Priority bar color
        val barColor = when (task.prioridad) {
            "ALTA" -> ContextCompat.getColor(ctx, R.color.priorityHighText)
            "BAJA" -> ContextCompat.getColor(ctx, R.color.priorityLowText)
            else -> ContextCompat.getColor(ctx, R.color.colorPrimary)
        }
        holder.viewBar.setBackgroundColor(barColor)

        // Assignee avatar
        holder.avatarAssignee.initials = userInitials
        holder.avatarAssignee.setColorForIndex(position % 6)

        holder.itemView.setOnClickListener { onItemClick(task) }
    }

    private fun formatDate(timestamp: Long): String {
        val taskDate = Calendar.getInstance().apply { timeInMillis = timestamp }
        val today = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }

        val fmt = SimpleDateFormat("h:mm a", Locale("es", "ES"))
        val time = fmt.format(Date(timestamp))

        return when {
            isSameDay(taskDate, today) -> "Hoy · $time"
            isSameDay(taskDate, tomorrow) -> "Mañana · $time"
            else -> {
                val dateFmt = SimpleDateFormat("MMM d", Locale("es", "ES"))
                "${dateFmt.format(Date(timestamp))} · $time"
            }
        }
    }

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
