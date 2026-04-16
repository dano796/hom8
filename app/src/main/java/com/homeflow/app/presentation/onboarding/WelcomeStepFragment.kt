package com.homeflow.app.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.homeflow.app.R

class WelcomeStepFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_onboarding_welcome, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val onboarding = parentFragment as? OnboardingFragment

        // "CREATE ACCOUNT" → Sign Up screen (step 2)
        view.findViewById<MaterialButton>(R.id.btnCreateAccount).setOnClickListener {
            onboarding?.navigateToStep(2)
        }

        // "I ALREADY HAVE AN ACCOUNT" → Sign In screen (step 1)
        view.findViewById<MaterialButton>(R.id.btnHaveAccount).setOnClickListener {
            onboarding?.navigateToStep(1)
        }
    }
}
