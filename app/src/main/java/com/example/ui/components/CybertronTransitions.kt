package com.example.ui.components

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import com.example.AppScreen

/**
 * Cybertron Bumblebee Custom AnimatedContent Transition Set:
 * Emulates the transformation, mechanical folding, and high-contrast yellow-and-black accent reveals
 * characteristic of the Autobot Bumblebee Cybertronian aesthetic.
 *
 * Mechanical Dynamics:
 * - Forward Navigation: Mechanical fold-in with high-speed horizontal armor slide and depth scaling.
 * - Back Navigation: Armor panel unfurl with reverse slide and scale decompression.
 * - Test / Practice Entry: High-voltage vertical Energon blast reveal.
 * - Results / Review: Cybernetic shutter reveal with focal scale expansion.
 */
object CybertronTransitions {

    private const val DURATION_TRANSFORM = 380
    private const val DURATION_FAST = 260

    fun cybertronTransform(
        isNavigatingForward: Boolean = true
    ): ContentTransform {
        val enterSlide = if (isNavigatingForward) {
            slideInHorizontally(
                animationSpec = tween(DURATION_TRANSFORM, easing = FastOutSlowInEasing),
                initialOffsetX = { fullWidth -> (fullWidth * 0.35f).toInt() }
            )
        } else {
            slideInHorizontally(
                animationSpec = tween(DURATION_TRANSFORM, easing = FastOutSlowInEasing),
                initialOffsetX = { fullWidth -> -(fullWidth * 0.35f).toInt() }
            )
        }

        val enterScale = scaleIn(
            animationSpec = tween(DURATION_TRANSFORM, easing = FastOutSlowInEasing),
            initialScale = if (isNavigatingForward) 0.88f else 1.08f
        )

        val enterFade = fadeIn(
            animationSpec = tween(DURATION_FAST, easing = LinearOutSlowInEasing)
        )

        val exitSlide = if (isNavigatingForward) {
            slideOutHorizontally(
                animationSpec = tween(DURATION_TRANSFORM, easing = FastOutSlowInEasing),
                targetOffsetX = { fullWidth -> -(fullWidth * 0.30f).toInt() }
            )
        } else {
            slideOutHorizontally(
                animationSpec = tween(DURATION_TRANSFORM, easing = FastOutSlowInEasing),
                targetOffsetX = { fullWidth -> (fullWidth * 0.30f).toInt() }
            )
        }

        val exitScale = scaleOut(
            animationSpec = tween(DURATION_TRANSFORM, easing = FastOutSlowInEasing),
            targetScale = if (isNavigatingForward) 1.08f else 0.88f
        )

        val exitFade = fadeOut(
            animationSpec = tween(DURATION_FAST, easing = FastOutLinearInEasing)
        )

        return (enterSlide + enterScale + enterFade).togetherWith(exitSlide + exitScale + exitFade)
    }

    /**
     * Specialized transition router between AppScreens based on hierarchy depth.
     */
    fun appScreenTransition(
        initialState: AppScreen,
        targetState: AppScreen
    ): ContentTransform {
        val screenRanks = mapOf(
            AppScreen.HOME to 0,
            AppScreen.PROFILE_HISTORY to 1,
            AppScreen.EXAM_PRACTICE to 2,
            AppScreen.TEST_INSTRUCTIONS to 3,
            AppScreen.PRACTICE_SCREEN to 4,
            AppScreen.TEST_SCREEN to 4,
            AppScreen.RESULT_SCREEN to 5,
            AppScreen.REVIEW_SCREEN to 6
        )

        val initialRank = screenRanks[initialState] ?: 0
        val targetRank = screenRanks[targetState] ?: 0

        // Vertical mechanical drop/lift for Test & Results
        if (targetState == AppScreen.TEST_SCREEN || targetState == AppScreen.PRACTICE_SCREEN) {
            return (slideInVertically(
                animationSpec = tween(DURATION_TRANSFORM, easing = FastOutSlowInEasing),
                initialOffsetY = { fullHeight -> (fullHeight * 0.25f).toInt() }
            ) + scaleIn(
                animationSpec = tween(DURATION_TRANSFORM, easing = FastOutSlowInEasing),
                initialScale = 0.90f
            ) + fadeIn(
                animationSpec = tween(DURATION_FAST, easing = LinearOutSlowInEasing)
            )).togetherWith(
                slideOutVertically(
                    animationSpec = tween(DURATION_TRANSFORM, easing = FastOutSlowInEasing),
                    targetOffsetY = { fullHeight -> -(fullHeight * 0.15f).toInt() }
                ) + fadeOut(
                    animationSpec = tween(DURATION_FAST, easing = FastOutLinearInEasing)
                )
            )
        }

        if (initialState == AppScreen.TEST_SCREEN || initialState == AppScreen.PRACTICE_SCREEN) {
            if (targetState == AppScreen.RESULT_SCREEN) {
                return (scaleIn(
                    animationSpec = tween(DURATION_TRANSFORM, easing = FastOutSlowInEasing),
                    initialScale = 0.85f
                ) + fadeIn(
                    animationSpec = tween(DURATION_FAST, easing = LinearOutSlowInEasing)
                )).togetherWith(
                    scaleOut(
                        animationSpec = tween(DURATION_FAST, easing = FastOutSlowInEasing),
                        targetScale = 1.05f
                    ) + fadeOut(
                        animationSpec = tween(DURATION_FAST, easing = FastOutLinearInEasing)
                    )
                )
            }
        }

        val isForward = targetRank >= initialRank
        return cybertronTransform(isNavigatingForward = isForward)
    }
}
