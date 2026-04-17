package com.hom8.app.presentation.dashboard.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hom8.app.R
import com.hom8.app.data.local.entity.TaskEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TodayTasksAdapter(
    private val onToggleDone: (TaskEntity) -> Unit,
    private val onItemClick: (TaskEntity) -> Unit
) : ListAdapter<TaskEntity, TodayTasksAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cbDone: CheckBox = view.findViewById(R.id.cbTaskDone)
        val tvTitle: TextView = view.findViewById(R.id.tvTaskTitle)
        val tvDueTime: TextView = view.findViewById(R.id.tvDueTime)
        val tvPriority: TextView = view.findViewById(R.id.tvPriority)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task_compact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = getItem(position)
        val ctx = holder.itemView.context
        val isDone = task.estado == "TERMINADO"

        holder.tvTitle.text = task.titulo
        holder.cbDone.isChecked = isDone

        // Strike-through when done
        if (isDone) {
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvTitle.alpha = 0.5f
        } else {
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.tvTitle.alpha = 1f
        }

        // Due time
        if (task.fechaLimite != null) {
            val fmt = SimpleDateFormat("h:mm a", Locale("es", "ES"))
            holder.tvDueTime.text = "Para ${fmt.format(Date(task.fechaLimite))}"
            holder.tvDueTime.visibility = View.VISIBLE
            val ivDue = holder.itemView.findViewById<View>(R.id.ivDueIcon)
            ivDue?.visibility = View.VISIBLE
        } else {
            holder.tvDueTime.visibility = View.GONE
            val ivDue = holder.itemView.findViewById<View>(R.id.ivDueIcon)
            ivDue?.visibility = View.GONE
        }

        // Priority badge
        when (task.prioridad) {
            "ALTA" -> {
                holder.tvPriority.text = "ALTA"
                holder.tvPriority.background = ContextCompat.getDrawable(ctx, R.drawable.bg_priority_high)
                holder.tvPriority.setTextColor(ContextCompat.getColor(ctx, R.color.priorityHighText))
            }
            "BAJA" -> {
                holder.tvPriority.text = "BAJA"
                holder.tvPriority.background = ContextCompat.getDrawable(ctx, R.drawable.bg_priority_low)
                holder.tvPriority.setTextColor(ContextCompat.getColor(ctx, R.color.priorityLowText))
            }
            else -> {
                holder.tvPriority.text = "MEDIA"
                holder.tvPriority.background = ContextCompat.getDrawable(ctx, R.drawable.bg_priority_medium)
                holder.tvPriority.setTextColor(ContextCompat.getColor(ctx, R.color.priorityMediumText))
            }
        }

        holder.cbDone.setOnClickListener { onToggleDone(task) }
        holder.itemView.setOnClickListener { onItemClick(task) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<TaskEntity>() {
            override fun areItemsTheSame(old: TaskEntity, new: TaskEntity) = old.id == new.id
            override fun areContentsTheSame(old: TaskEntity, new: TaskEntity) = old == new
        }
    }
}
