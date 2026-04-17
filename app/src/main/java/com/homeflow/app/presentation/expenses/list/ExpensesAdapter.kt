package com.homeflow.app.presentation.expenses.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.homeflow.app.R
import com.homeflow.app.data.local.entity.ExpenseEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpensesAdapter(
    private val currentUserId: String,
    private var membersMap: Map<String, String> = emptyMap(),
    private var isSplitMode: Boolean = true,
    private val onItemClick: (ExpenseEntity) -> Unit,
    private val onDeleteClick: (ExpenseEntity) -> Unit
) : ListAdapter<ExpenseEntity, ExpensesAdapter.ViewHolder>(DIFF) {

    fun updateMembersMap(map: Map<String, String>) {
        membersMap = map
        notifyItemRangeChanged(0, itemCount)
    }

    fun updateSplitMode(splitMode: Boolean) {
        if (isSplitMode != splitMode) {
            isSplitMode = splitMode
            notifyItemRangeChanged(0, itemCount)
        }
    }

    private val currencyFmt = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    private val dateFmt = SimpleDateFormat("MMM d", Locale("es", "ES"))

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmoji: TextView = view.findViewById(R.id.tvCategoryEmoji)
        val tvDescription: TextView = view.findViewById(R.id.tvExpenseDescription)
        val tvAmount: TextView = view.findViewById(R.id.tvExpenseAmount)
        val tvPaidBy: TextView = view.findViewById(R.id.tvPaidBy)
        val tvYourShare: TextView = view.findViewById(R.id.tvYourShare)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = getItem(position)

        holder.tvEmoji.text = categoryEmoji(expense.categoria)
        holder.tvDescription.text = expense.descripcion
        holder.tvAmount.text = currencyFmt.format(expense.monto)

        val paidByName = if (expense.pagadorId == currentUserId) {
            "Tú"
        } else {
            membersMap[expense.pagadorId] ?: expense.pagadorId.take(8).ifEmpty { "Miembro" }
        }
        val dateStr = dateFmt.format(Date(expense.fecha))
        holder.tvPaidBy.text = "Pagado por $paidByName · $dateStr"

        if (isSplitMode) {
            val participantCount = parseParticipantCount(expense.participantes)
            val share = expense.monto / participantCount.coerceAtLeast(1)
            holder.tvYourShare.text = "Tu parte: ${currencyFmt.format(share)}"
            holder.tvYourShare.visibility = View.VISIBLE
        } else {
            holder.tvYourShare.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onItemClick(expense) }
        holder.itemView.setOnLongClickListener {
            onDeleteClick(expense)
            true
        }
    }

    private fun categoryEmoji(categoria: String): String = when (categoria) {
        "COMIDA" -> "🍕"
        "SUPERMERCADO" -> "🛒"
        "SERVICIOS" -> "💡"
        "TRANSPORTE" -> "🚗"
        "OCIO" -> "🎬"
        "LIMPIEZA" -> "🧹"
        "SALUD" -> "💊"
        else -> "💸"
    }

    private fun parseParticipantCount(json: String): Int {
        return try {
            if (json == "[]") 1 else json.split(",").size
        } catch (e: Exception) {
            1
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ExpenseEntity>() {
            override fun areItemsTheSame(old: ExpenseEntity, new: ExpenseEntity) = old.id == new.id
            override fun areContentsTheSame(old: ExpenseEntity, new: ExpenseEntity) = old == new
        }
    }
}
