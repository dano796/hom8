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
import com.homeflow.app.presentation.common.WireAvatar
import com.homeflow.app.presentation.dashboard.ActivityItem

class ActivityFeedAdapter : ListAdapter<ActivityItem, ActivityFeedAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: WireAvatar = view.findViewById(R.id.avatarActor)
        val dotView: View = view.findViewById(R.id.viewActivityDot)
        val tvText: TextView = view.findViewById(R.id.tvActivityText)
        val tvTime: TextView = view.findViewById(R.id.tvActivityTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity_feed, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val ctx = holder.itemView.context

        holder.avatar.initials = item.actorInitials
        holder.avatar.setColorForIndex(item.actorColorIndex)
        holder.dotView.background.setTint(ContextCompat.getColor(ctx, item.dotColor))
        holder.tvText.text = item.text
        holder.tvTime.text = item.timeAgo
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ActivityItem>() {
            override fun areItemsTheSame(old: ActivityItem, new: ActivityItem) = old.text == new.text
            override fun areContentsTheSame(old: ActivityItem, new: ActivityItem) = old == new
        }
    }
}
