package com.hom8.app.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.hom8.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SetupHomeStepFragment : Fragment() {

    private val viewModel: OnboardingViewModel by viewModels({ requireParentFragment() })

    private var selectedMode: SetupMode? = null

    enum class SetupMode { CREATE, JOIN }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_onboarding_setup, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardCreate = view.findViewById<MaterialCardView>(R.id.cardCreateHome)
        val cardJoin = view.findViewById<MaterialCardView>(R.id.cardJoinHome)
        val layoutCreateForm = view.findViewById<LinearLayout>(R.id.layoutCreateHomeForm)
        val layoutJoinForm = view.findViewById<LinearLayout>(R.id.layoutJoinHomeForm)
        val btnContinue = view.findViewById<MaterialButton>(R.id.btnContinueSetup)
        val tvError = view.findViewById<TextView>(R.id.tvSetupError)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressSetup)
        val tilHomeName = view.findViewById<TextInputLayout>(R.id.tilHomeName)
        val etHomeName = view.findViewById<TextInputEditText>(R.id.etHomeName)
        val tilInviteCode = view.findViewById<TextInputLayout>(R.id.tilInviteCode)
        val etInviteCode = view.findViewById<TextInputEditText>(R.id.etInviteCode)

        cardCreate.setOnClickListener {
            selectedMode = SetupMode.CREATE
            layoutCreateForm.visibility = View.VISIBLE
            layoutJoinForm.visibility = View.GONE
            btnContinue.isEnabled = true
            // Highlight selected card
            cardCreate.strokeColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            cardCreate.strokeWidth = 2
            cardJoin.strokeColor = ContextCompat.getColor(requireContext(), R.color.cardBorder)
            cardJoin.strokeWidth = 1
        }

        cardJoin.setOnClickListener {
            selectedMode = SetupMode.JOIN
            layoutJoinForm.visibility = View.VISIBLE
            layoutCreateForm.visibility = View.GONE
            btnContinue.isEnabled = true
            // Highlight selected card
            cardJoin.strokeColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            cardJoin.strokeWidth = 2
            cardCreate.strokeColor = ContextCompat.getColor(requireContext(), R.color.cardBorder)
            cardCreate.strokeWidth = 1
        }

        // Clear any lingering errors when the user starts editing
        etHomeName.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tilHomeName.error = null
                tvError.visibility = View.GONE
            }
        })
        etInviteCode.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tilInviteCode.error = null
                tvError.visibility = View.GONE
            }
        })

        btnContinue.setOnClickListener {
            // Clear stale errors before each attempt
            tilHomeName.error = null
            tilInviteCode.error = null
            tvError.visibility = View.GONE
            viewModel.clearError()

            when (selectedMode) {
                SetupMode.CREATE -> {
                    val name = etHomeName.text?.toString()?.trim() ?: ""
                    if (name.isEmpty()) {
                        tilHomeName.error = "El nombre del hogar es requerido"
                        return@setOnClickListener
                    }
                    viewModel.createHome(name)
                }
                SetupMode.JOIN -> {
                    val code = etInviteCode.text?.toString()?.trim()?.uppercase() ?: ""
                    if (code.isEmpty()) {
                        tilInviteCode.error = "El código de invitación es requerido"
                        return@setOnClickListener
                    }
                    viewModel.joinHome(code)
                }
                null -> { /* no card selected */ }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    btnContinue.isEnabled = !state.isLoading && selectedMode != null

                    if (state.error != null) {
                        // Show inline on the active field so it's always visible, and in the banner
                        when (selectedMode) {
                            SetupMode.JOIN -> {
                                tilInviteCode.error = state.error
                                // Scroll to the input so the inline error is visible
                                tilInviteCode.requestFocus()
                            }
                            SetupMode.CREATE -> {
                                tilHomeName.error = state.error
                                tilHomeName.requestFocus()
                            }
                            else -> {
                                tvError.text = state.error
                                tvError.visibility = View.VISIBLE
                            }
                        }
                        // Don't call clearError() here — it will be cleared on next action
                    }
                }
            }
        }
    }
}
