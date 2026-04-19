package com.hom8.app.presentation.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hom8.app.R
import com.hom8.app.data.local.entity.NotificationEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.os.Bundle

@AndroidEntryPoint
class NotificationsFragment : Fragment() {

    private val viewModel: NotificationsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_notifications, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvNotifications = view.findViewById<RecyclerView>(R.id.rvNotifications)
        val layoutEmpty = view.findViewById<LinearLayout>(R.id.layoutNotificationsEmpty)
        val tvMarkAll = view.findViewById<TextView>(R.id.tvMarkAllRead)

        view.findViewById<ImageButton>(R.id.btnNotificationsBack).setOnClickListener {
            findNavController().popBackStack()
        }

        tvMarkAll.setOnClickListener {
            viewModel.markAllAsRead()
        }

        val adapter = NotificationsAdapter()
        rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        rvNotifications.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.notifications.collect { notifications ->
                    adapter.submitList(notifications)
                    layoutEmpty.visibility = if (notifications.isEmpty()) View.VISIBLE else View.GONE
                    rvNotifications.visibility = if (notifications.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }
}

class NotificationsAdapter :
    androidx.recyclerview.widget.ListAdapter<NotificationEntity, NotificationsAdapter.VH>(
        object : androidx.recyclerview.widget.DiffUtil.ItemCallback<NotificationEntity>() {
            override fun areItemsTheSame(a: NotificationEntity, b: NotificationEntity) = a.id == b.id
            override fun areContentsTheSame(a: NotificationEntity, b: NotificationEntity) = a == b
        }
    ) {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val dot: View = view.findViewById(R.id.viewUnreadDot)
        val title: TextView = view.findViewById(R.id.tvNotifTitle)
        val subtitle: TextView = view.findViewById(R.id.tvNotifSubtitle)
        val time: TextView = view.findViewById(R.id.tvNotifTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val notif = getItem(position)
        holder.title.text = notif.title
        if (notif.subtitle.isNotBlank()) {
            holder.subtitle.text = notif.subtitle
            holder.subtitle.visibility = View.VISIBLE
        } else {
            holder.subtitle.visibility = View.GONE
        }
        holder.dot.visibility = if (notif.unread == 1) View.VISIBLE else View.INVISIBLE
        holder.time.text = formatTime(notif.timestamp)
    }

    private fun formatTime(ts: Long): String {
        val diff = System.currentTimeMillis() - ts
        val minutes = diff / 60_000
        val hours = minutes / 60
        val days = hours / 24
        return when {
            minutes < 1 -> "justo ahora"
            minutes < 60 -> "hace ${minutes}m"
            hours < 24 -> "hace ${hours}h"
            days == 1L -> "Ayer"
            days < 7 -> "hace ${days}d"
            else -> SimpleDateFormat("d MMM yyyy", Locale("es", "ES")).format(Date(ts))
        }
    }
}
