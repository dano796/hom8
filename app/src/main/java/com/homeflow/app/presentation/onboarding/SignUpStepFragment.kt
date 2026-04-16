package com.homeflow.app.presentation.onboarding

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
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.homeflow.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpStepFragment : Fragment() {

    private val viewModel: OnboardingViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_onboarding_signup, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etName = view.findViewById<TextInputEditText>(R.id.etName)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmailSignUp)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPasswordSignUp)
        val tilName = view.findViewById<TextInputLayout>(R.id.tilName)
        val tilEmail = view.findViewById<TextInputLayout>(R.id.tilEmailSignUp)
        val tilPassword = view.findViewById<TextInputLayout>(R.id.tilPasswordSignUp)
        val tvError = view.findViewById<TextView>(R.id.tvSignUpError)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressSignUp)
        val btnCreate = view.findViewById<MaterialButton>(R.id.btnCreateAccount)

        // Back → Welcome
        view.findViewById<ImageButton>(R.id.btnBackSignUp).setOnClickListener {
            (parentFragment as? OnboardingFragment)?.navigateToStep(0)
        }

        // Create account
        btnCreate.setOnClickListener {
            val name = etName.text?.toString()?.trim() ?: ""
            val email = etEmail.text?.toString()?.trim() ?: ""
            val password = etPassword.text?.toString() ?: ""

            var valid = true
            if (name.isBlank()) {
                tilName.error = "Name is required"
                valid = false
            } else tilName.error = null

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.error = "Enter a valid email address"
                valid = false
            } else tilEmail.error = null

            if (password.length < 6) {
                tilPassword.error = "At least 6 characters"
                valid = false
            } else tilPassword.error = null

            if (valid) viewModel.signUpWithEmail(email, password, name)
        }

        // Already have account → Sign In
        view.findViewById<TextView>(R.id.tvGoToSignIn).setOnClickListener {
            (parentFragment as? OnboardingFragment)?.navigateToStep(1)
        }

        // Observe state
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    btnCreate.isEnabled = !state.isLoading

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
}
