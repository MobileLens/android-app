package com.mobilelens.mobilelens.core.ui.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * Timing shared by the app's animations, from Material 3's motion tokens
 * (m3.material.io/styles/motion), so everything moves with the same rhythm.
 */
object Motion {
    // For things that stay on screen and change
    val Emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    // For things coming onto the screen: fast at first, then settling
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    // For things leaving the screen: slow at first, then gone
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    const val DURATION_SHORT = 150
    const val DURATION_MEDIUM = 300
    const val DURATION_LONG = 400

    /** How far content travels when it slides between screens or tabs. */
    val SlideDistance = 30.dp
}

// Fade through and shared axis share one split: the old content is gone after the first 90 ms and
// the new one fades in over the rest, so the two are hardly ever on screen together
private const val FADE_OUT_MILLIS = 90
private const val FADE_IN_MILLIS = Motion.DURATION_MEDIUM - FADE_OUT_MILLIS

private const val SHEET_EXIT_MILLIS = 250

/** [Motion.SlideDistance] in pixels, for slide transitions that take an offset. */
@Composable
fun rememberSlideDistance(): Int {
    val density = LocalDensity.current
    return remember(density) { with(density) { Motion.SlideDistance.roundToPx() } }
}

/**
 * Fade through, for content that replaces unrelated content: fades (and optionally scales) in once
 * [fadeThroughOut] has cleared the old content away.
 */
fun fadeThroughIn(initialScale: Float = 1f): EnterTransition {
    val spec = tween<Float>(FADE_IN_MILLIS, delayMillis = FADE_OUT_MILLIS, easing = Motion.EmphasizedDecelerate)
    return fadeIn(spec) + scaleIn(spec, initialScale)
}

fun fadeThroughOut(): ExitTransition =
    fadeOut(tween(FADE_OUT_MILLIS, easing = Motion.EmphasizedAccelerate))

/**
 * Shared axis X, for moving to the next or previous of related contents (a screen opened from this
 * one, the next tab). [forward] content comes in from the end side.
 */
fun sharedAxisXIn(forward: Boolean, slideDistance: Int): EnterTransition =
    slideInHorizontally(tween(Motion.DURATION_MEDIUM, easing = Motion.Emphasized)) {
        if (forward) slideDistance else -slideDistance
    } + fadeIn(tween(FADE_IN_MILLIS, delayMillis = FADE_OUT_MILLIS, easing = Motion.EmphasizedDecelerate))

fun sharedAxisXOut(forward: Boolean, slideDistance: Int): ExitTransition =
    slideOutHorizontally(tween(Motion.DURATION_MEDIUM, easing = Motion.Emphasized)) {
        if (forward) -slideDistance else slideDistance
    } + fadeOut(tween(FADE_OUT_MILLIS, easing = Motion.EmphasizedAccelerate))

/** A full-screen sheet rising from the bottom edge over the content that opened it. */
fun sheetIn(): EnterTransition =
    slideInVertically(tween(Motion.DURATION_LONG, easing = Motion.EmphasizedDecelerate)) { it }

fun sheetOut(): ExitTransition =
    slideOutVertically(tween(SHEET_EXIT_MILLIS, easing = Motion.EmphasizedAccelerate)) { it }
