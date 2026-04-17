package com.homeflow.app.presentation.common

import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator

/**
 * Extension functions for premium UI interactions
 */

/**
 * Add scale animation on button press for premium feel
 */
fun View.addPressAnimation() {
    setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .start()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                v.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .setInterpolator(OvershootInterpolator())
                    .start()
            }
        }
        false
    }
}

/**
 * Add haptic feedback on click
 */
fun View.addHapticClick(feedbackType: Int = HapticFeedbackConstants.CONTEXT_CLICK) {
    setOnClickListener { view ->
        view.performHapticFeedback(feedbackType)
    }
}

/**
 * Combine press animation with haptic feedback
 */
fun View.addPremiumClick(
    feedbackType: Int = HapticFeedbackConstants.CONTEXT_CLICK,
    onClick: () -> Unit
) {
    setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .start()
            }
            MotionEvent.ACTION_UP -> {
                v.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .setInterpolator(OvershootInterpolator())
                    .start()
                
                v.performHapticFeedback(feedbackType)
                onClick()
            }
            MotionEvent.ACTION_CANCEL -> {
                v.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
        }
        true
    }
}

/**
 * Add hover effect for tablets (elevation change)
 */
fun View.addHoverEffect() {
    setOnHoverListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_HOVER_ENTER -> {
                v.animate()
                    .translationZ(8f)
                    .setDuration(150)
                    .start()
            }
            MotionEvent.ACTION_HOVER_EXIT -> {
                v.animate()
                    .translationZ(0f)
                    .setDuration(150)
                    .start()
            }
        }
        false
    }
}

/**
 * Fade in animation
 */
fun View.fadeIn(duration: Long = 300, startDelay: Long = 0) {
    alpha = 0f
    animate()
        .alpha(1f)
        .setDuration(duration)
        .setStartDelay(startDelay)
        .start()
}

/**
 * Fade out animation
 */
fun View.fadeOut(duration: Long = 300, startDelay: Long = 0) {
    animate()
        .alpha(0f)
        .setDuration(duration)
        .setStartDelay(startDelay)
        .start()
}

/**
 * Slide in from bottom animation
 */
fun View.slideInFromBottom(duration: Long = 300, startDelay: Long = 0) {
    translationY = 100f
    alpha = 0f
    animate()
        .translationY(0f)
        .alpha(1f)
        .setDuration(duration)
        .setStartDelay(startDelay)
        .setInterpolator(android.view.animation.DecelerateInterpolator())
        .start()
}

/**
 * Scale and fade in animation
 */
fun View.scaleIn(duration: Long = 300, startDelay: Long = 0) {
    scaleX = 0.8f
    scaleY = 0.8f
    alpha = 0f
    animate()
        .scaleX(1f)
        .scaleY(1f)
        .alpha(1f)
        .setDuration(duration)
        .setStartDelay(startDelay)
        .setInterpolator(OvershootInterpolator())
        .start()
}
