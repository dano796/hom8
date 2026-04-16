package com.homeflow.app.presentation.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.homeflow.app.R
import com.homeflow.app.data.remote.FirestoreSyncManager
import com.homeflow.app.util.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment() {

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var session: SessionManager

    @Inject
    lateinit var syncManager: FirestoreSyncManager

    override fun onDestroyView() {
        super.onDestroyView()
        syncManager.stopSync()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_main, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check auth — if not logged in, go to onboarding
        if (auth.currentUser == null && !session.isLoggedIn) {
            findNavController().navigate(R.id.action_main_to_onboarding)
            return
        }

        // Sync Firebase user to session if needed
        auth.currentUser?.let { firebaseUser ->
            if (session.userId.isEmpty()) {
                session.userId = firebaseUser.uid
                session.userName = firebaseUser.displayName
                    ?: firebaseUser.email?.substringBefore("@") ?: "User"
                session.userInitials = session.userName
                    .split(" ")
                    .mapNotNull { it.firstOrNull()?.uppercase() }
                    .take(2)
                    .joinToString("")
            }
        }

        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.nav_host_bottom) as NavHostFragment
        val navController = navHostFragment.navController

        // Start Firestore real-time sync for the active home
        val hogarId = session.hogarId
        if (hogarId.isNotEmpty()) syncManager.startSync(hogarId)

        val bottomNav = view.findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setupWithNavController(navController)

        // Apply window insets
        val statusBarScrim = view.findViewById<View>(R.id.statusBarScrim)
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            // Expand the scrim to exactly the status bar height so top bars
            // in fragments visually connect to the status bar seamlessly
            statusBarScrim.updateLayoutParams { height = statusBars.top }

            // Add gesture / button nav bar padding below the bottom navigation
            bottomNav.updatePadding(bottom = navBars.bottom)

            insets
        }
    }
}
