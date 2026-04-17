package com.hom8.app.presentation.members

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hom8.app.R
import com.hom8.app.presentation.common.WireAvatar

class MembersAdapter : ListAdapter<MemberItem, MembersAdapter.MemberViewHolder>(DiffCallback) {

    inner class MemberViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val avatar: WireAvatar = view.findViewById(R.id.avatarMember)
        private val tvName: TextView = view.findViewById(R.id.tvMemberName)
        private val tvEmail: TextView = view.findViewById(R.id.tvMemberEmail)
        private val tvRole: TextView = view.findViewById(R.id.tvMemberRole)
        private val tvYou: TextView = view.findViewById(R.id.tvMemberYou)

        fun bind(item: MemberItem) {
            avatar.initials = item.initials
            avatar.setColorForIndex(item.colorIndex)
            tvName.text = item.name
            tvEmail.text = item.email.ifEmpty { "—" }
            tvRole.text = item.role
            tvYou.visibility = if (item.isCurrentUser) View.VISIBLE else View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<MemberItem>() {
            override fun areItemsTheSame(a: MemberItem, b: MemberItem) = a.id == b.id
            override fun areContentsTheSame(a: MemberItem, b: MemberItem) = a == b
        }
    }
}
