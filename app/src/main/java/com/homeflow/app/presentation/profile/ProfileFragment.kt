package com.homeflow.app.presentation.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.homeflow.app.R
import com.homeflow.app.presentation.common.WireAvatar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialButton>(R.id.btnManageHomes).setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_manageHomes)
        }

        view.findViewById<MaterialButton>(R.id.btnViewMembers).setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_houseMembers)
        }

        view.findViewById<MaterialButton>(R.id.btnLogOut).setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out") { _, _ ->
                    viewModel.logOut()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        view.findViewById<TextView>(R.id.tvCopyCode).setOnClickListener {
            val code = view.findViewById<TextView>(R.id.tvInviteCode).text.toString()
            if (code.isNotEmpty() && code != "-") {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Invite Code", code))
                Snackbar.make(requireView(), "Invite code copied!", Snackbar.LENGTH_SHORT).show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // User info
                    view.findViewById<WireAvatar>(R.id.avatarProfile).apply {
                        initials = state.userInitials
                        setColorForIndex(0)
                    }
                    view.findViewById<TextView>(R.id.tvProfileName).text = state.userName
                    view.findViewById<TextView>(R.id.tvProfileEmail).text = state.userEmail
                    view.findViewById<TextView>(R.id.tvMemberSince).text = state.memberSince

                    // Stats
                    view.findViewById<TextView>(R.id.tvTasksDone).text = state.tasksDone.toString()
                    view.findViewById<TextView>(R.id.tvStreak).text = "${state.streakDays}d"
                    view.findViewById<TextView>(R.id.tvScore).text = state.score.toString()

                    // Home info
                    view.findViewById<TextView>(R.id.tvHomeName).text = state.homeName
                    view.findViewById<TextView>(R.id.tvInviteCode).text = state.inviteCode

                    // Navigate to onboarding on logout — use parent nav controller
                    if (state.loggedOut) {
                        // The parent NavController handles root-level nav
                        requireActivity().let { activity ->
                            val navController = androidx.navigation.Navigation.findNavController(
                                activity, R.id.nav_host_fragment
                            )
                            navController.navigate(R.id.action_main_to_onboarding)
                        }
                    }
                }
            }
        }
    }
}
