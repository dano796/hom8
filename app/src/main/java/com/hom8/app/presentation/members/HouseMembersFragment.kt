package com.hom8.app.presentation.members

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.hom8.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HouseMembersFragment : Fragment() {

    private val viewModel: HouseMembersViewModel by viewModels()
    private lateinit var adapter: MembersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_house_members, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MembersAdapter()
        view.findViewById<RecyclerView>(R.id.rvMembers).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HouseMembersFragment.adapter
        }

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<TextView>(R.id.tvCopyInviteCode).setOnClickListener {
            val code = view.findViewById<TextView>(R.id.tvMembersInviteCode).text.toString()
            if (code.isNotEmpty() && code != "-") {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Código de invitación", code))
                Snackbar.make(requireView(), getString(R.string.members_code_copied), Snackbar.LENGTH_SHORT).show()
            }
        }

        view.findViewById<TextView>(R.id.tvShareInviteLink).setOnClickListener {
            val code = view.findViewById<TextView>(R.id.tvMembersInviteCode).text.toString()
            if (code.isNotEmpty() && code != "-") {
                shareInviteLink(code, view.findViewById<TextView>(R.id.tvMembersHomeName).text.toString())
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    view.findViewById<TextView>(R.id.tvMembersHomeName).text = state.homeName
                    view.findViewById<TextView>(R.id.tvMembersCount).text =
                        getString(R.string.members_count, state.members.size)
                    view.findViewById<TextView>(R.id.tvMembersInviteCode).text =
                        state.inviteCode.ifEmpty { "-" }

                    val emptyView = view.findViewById<View>(R.id.emptyMembers)
                    val rvMembers = view.findViewById<View>(R.id.rvMembers)
                    if (state.members.isEmpty() && !state.isLoading) {
                        emptyView.visibility = View.VISIBLE
                        rvMembers.visibility = View.GONE
                    } else {
                        emptyView.visibility = View.GONE
                        rvMembers.visibility = View.VISIBLE
                    }

                    adapter.submitList(state.members)
                }
            }
        }
    }

    private fun shareInviteLink(inviteCode: String, homeName: String) {
        val inviteLink = "https://hom8.app/join/$inviteCode"
        
        val shareText = """
            ¡Únete a $homeName en Hom8! 🏠
            
            Usa este link para unirte:
            $inviteLink
            
            O ingresa el código: $inviteCode
        """.trimIndent()
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_TITLE, "Invitación a Hom8")
        }
        
        startActivity(Intent.createChooser(shareIntent, "Compartir invitación"))
    }
}
