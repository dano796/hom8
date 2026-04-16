package com.homeflow.app.presentation.homes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.homeflow.app.R
import com.homeflow.app.data.local.entity.HomeEntity

class HomeCardAdapter(
    private val onSwitchClick: (HomeEntity) -> Unit
) : ListAdapter<HomeEntity, HomeCardAdapter.ViewHolder>(DIFF) {

    private var activeHomeId: String = ""

    fun setActiveHomeId(id: String) {
        activeHomeId = id
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val card: MaterialCardView = view.findViewById(R.id.cardHome)
        private val tvName: TextView = view.findViewById(R.id.tvHomeCardName)
        private val tvCode: TextView = view.findViewById(R.id.tvHomeCardCode)
        private val tvMembers: TextView = view.findViewById(R.id.tvHomeCardMembers)
        private val tvActive: TextView = view.findViewById(R.id.tvActiveIndicator)
        private val ivSwitch: ImageView = view.findViewById(R.id.ivSwitch)

        fun bind(home: HomeEntity) {
            tvName.text = home.nombre
            tvCode.text = home.codigoInvitacion
            val memberCount = parseMemberCount(home.miembros)
            tvMembers.text = "$memberCount member${if (memberCount != 1) "s" else ""}"

            val isActive = home.id == activeHomeId
            tvActive.visibility = if (isActive) View.VISIBLE else View.GONE
            ivSwitch.visibility = if (isActive) View.GONE else View.VISIBLE

            if (isActive) {
                card.strokeWidth = 2
                card.strokeColor = card.context.getColor(R.color.colorPrimary)
                card.isClickable = false
            } else {
                card.strokeWidth = 0
                card.isClickable = true
                card.setOnClickListener { onSwitchClick(home) }
            }
        }

        private fun parseMemberCount(json: String): Int {
            val trimmed = json.trim().removePrefix("[").removeSuffix("]")
            if (trimmed.isBlank()) return 0
            return trimmed.split(",").count { it.isNotBlank() }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<HomeEntity>() {
            override fun areItemsTheSame(a: HomeEntity, b: HomeEntity) = a.id == b.id
            override fun areContentsTheSame(a: HomeEntity, b: HomeEntity) = a == b
        }
    }
}
