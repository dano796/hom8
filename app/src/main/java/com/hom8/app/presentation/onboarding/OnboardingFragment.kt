package com.hom8.app.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.hom8.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OnboardingFragment : Fragment() {

    private val viewModel: OnboardingViewModel by viewModels()

    private lateinit var viewPager: ViewPager2
    private lateinit var indicator1: View
    private lateinit var indicator2: View
    private lateinit var indicator3: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_onboarding, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = view.findViewById(R.id.viewPager)
        indicator1 = view.findViewById(R.id.indicator1)
        indicator2 = view.findViewById(R.id.indicator2)
        indicator3 = view.findViewById(R.id.indicator3)

        val adapter = OnboardingPagerAdapter(this)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false

        // Apply status bar inset to the scrim
        val scrim = view.findViewById<View>(R.id.onboardingStatusBarScrim)
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            scrim.updateLayoutParams { height = statusBars.top }
            insets
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // Pages 1 and 2 (Sign In / Sign Up) both light up the middle dot
                updateIndicators(
                    dotIndex = when (position) {
                        0 -> 0
                        1, 2 -> 1
                        else -> 2
                    }
                )
            }
        })

        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (viewPager.currentItem != state.currentStep) {
                        viewPager.currentItem = state.currentStep
                    }
                    if (state.isSetupComplete) {
                        findNavController().navigate(R.id.action_onboarding_to_main)
                    }
                }
            }
        }
    }

    fun navigateToStep(step: Int) {
        viewModel.goToStep(step)
    }

    private fun updateIndicators(dotIndex: Int) {
        val activeWidth = resources.getDimensionPixelSize(R.dimen.indicator_active_width)
        val inactiveWidth = resources.getDimensionPixelSize(R.dimen.indicator_inactive_width)
        val activeColor = requireContext().getColor(R.color.colorPrimary)
        val inactiveColor = requireContext().getColor(R.color.colorOutline)

        val dots = listOf(indicator1, indicator2, indicator3)
        dots.forEachIndexed { index, dot ->
            val lp = dot.layoutParams
            lp.width = if (index == dotIndex) activeWidth else inactiveWidth
            dot.layoutParams = lp
            dot.setBackgroundColor(if (index == dotIndex) activeColor else inactiveColor)
        }
    }
}
