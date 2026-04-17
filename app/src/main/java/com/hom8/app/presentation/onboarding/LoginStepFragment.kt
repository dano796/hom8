package com.hom8.app.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.hom8.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginStepFragment : Fragment() {

    private val viewModel: OnboardingViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_onboarding_login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)
        val tilEmail = view.findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPassword = view.findViewById<TextInputLayout>(R.id.tilPassword)
        val tvError = view.findViewById<TextView>(R.id.tvLoginError)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressLogin)
        val btnSignIn = view.findViewById<MaterialButton>(R.id.btnSignIn)

        // Back → Welcome
        view.findViewById<ImageButton>(R.id.btnBackSignIn).setOnClickListener {
            (parentFragment as? OnboardingFragment)?.navigateToStep(0)
        }

        // Sign In
        btnSignIn.setOnClickListener {
            val email = etEmail.text?.toString()?.trim() ?: ""
            val password = etPassword.text?.toString() ?: ""
            if (validateFields(email, password, tilEmail, tilPassword)) {
                viewModel.signInWithEmail(email, password)
            }
        }

        // Google (placeholder)
        view.findViewById<MaterialButton>(R.id.btnGoogleSignIn).setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.dialog_google_signin_title))
                .setMessage(getString(R.string.dialog_google_signin_message))
                .setPositiveButton(getString(R.string.dialog_ok), null)
                .show()
        }

        // Forgot password
        view.findViewById<TextView>(R.id.tvForgotPassword).setOnClickListener {
            val email = etEmail.text?.toString()?.trim() ?: ""
            if (email.isEmpty()) {
                tilEmail.error = getString(R.string.validation_email_required)
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.error = getString(R.string.validation_email_invalid)
                return@setOnClickListener
            }
            tilEmail.error = null
            viewModel.resetPassword(email)
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.dialog_reset_password_title))
                .setMessage(getString(R.string.dialog_reset_password_message, email))
                .setPositiveButton(getString(R.string.dialog_ok), null)
                .show()
        }

        // "Create account" → navigate to Sign Up screen (step 2)
        view.findViewById<TextView>(R.id.tvGoToRegister).setOnClickListener {
            (parentFragment as? OnboardingFragment)?.navigateToStep(2)
        }

        // Observe state
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    btnSignIn.isEnabled = !state.isLoading

                    if (state.error != null) {
                        tvError.text = state.error
                        tvError.visibility = View.VISIBLE
                        Snackbar.make(requireView(), state.error, Snackbar.LENGTH_LONG).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    private fun validateFields(
        email: String, password: String,
        tilEmail: TextInputLayout, tilPassword: TextInputLayout
    ): Boolean {
        var valid = true
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = getString(R.string.validation_email_invalid)
            valid = false
        } else tilEmail.error = null
        if (password.length < 6) {
            tilPassword.error = getString(R.string.validation_password_short)
            valid = false
        } else tilPassword.error = null
        return valid
    }
}
