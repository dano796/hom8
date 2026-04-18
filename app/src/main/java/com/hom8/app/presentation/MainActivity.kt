package com.hom8.app.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.hom8.app.R
import com.hom8.app.util.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Draw content edge-to-edge (required on Android 15 / API 35)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)
        
        // Handle deep link for home invitations
        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val data: Uri? = intent?.data
        if (data != null) {
            // Handle URLs like:
            // https://hom8.app/join/HF-ABC123
            // hom8://join/HF-ABC123
            val path = data.path
            if (path?.startsWith("/join") == true || data.host == "join") {
                val inviteCode = data.lastPathSegment
                if (!inviteCode.isNullOrBlank()) {
                    // Store the invite code in session to be processed by OnboardingViewModel
                    session.pendingInviteCode = inviteCode
                }
            }
        }
    }
}
