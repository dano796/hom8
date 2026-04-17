package com.hom8.app.presentation.onboarding

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnboardingPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    // 0 = Welcome, 1 = Sign In, 2 = Sign Up, 3 = Setup Home
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> WelcomeStepFragment()
        1 -> LoginStepFragment()
        2 -> SignUpStepFragment()
        3 -> SetupHomeStepFragment()
        else -> WelcomeStepFragment()
    }
}
