package com.mobilelens.mobilelens.core.ui.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEvent
import kotlin.math.max
import kotlin.math.roundToInt

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
    // For following the back gesture, as Material's components do: things respond as soon as the
    // gesture starts, then less and less the further it goes
    val PredictiveBack: Easing = CubicBezierEasing(0.1f, 0.1f, 0f, 1f)

    const val DURATION_SHORT = 150
    const val DURATION_MEDIUM = 300
    const val DURATION_LONG = 400

    /** How far content travels when it slides between screens or tabs. */
    val SlideDistance = 30.dp

    /** How close a screen the back gesture drags away gets to the edge it's heading for. */
    val PredictiveBackMargin = 8.dp

    /** The corners of a screen the back gesture drags away: Material's extra-large shape. */
    val PredictiveBackCornerRadius = 28.dp

    /** The shadow that lifts a screen the back gesture drags away off the one it uncovers. */
    val PredictiveBackElevation = 6.dp

    // Material 3 Expressive spatial springs: a slight overshoot for things that move or change shape
    fun <T> fastSpatialSpring(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.6f, stiffness = 800f)
    fun <T> defaultSpatialSpring(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.8f, stiffness = 380f)
}

// Fade through and shared axis share one split: the old content is gone after the first 90 ms and
// the new one fades in over the rest, so the two are hardly ever on screen together
private const val FADE_OUT_MILLIS = 90
private const val FADE_IN_MILLIS = Motion.DURATION_MEDIUM - FADE_OUT_MILLIS

private const val SHEET_EXIT_MILLIS = 250

// The back gesture scrubs through the predictive back transitions, and once it's let go they play
// the rest of the way at normal speed. So how far the gesture has gone is a point on this timeline.
private const val PREDICTIVE_BACK_MILLIS = Motion.DURATION_LONG
// As small as Material's predictive back makes a full-screen surface
private const val PREDICTIVE_BACK_SCALE = 0.9f

/** [Motion.SlideDistance] in pixels, for slide transitions that take an offset. */
@Composable
fun rememberSlideDistance(): Int {
    val density = LocalDensity.current
    return remember(density) { with(density) { Motion.SlideDistance.roundToPx() } }
}

/** [Motion.PredictiveBackMargin] in pixels. */
@Composable
fun rememberPredictiveBackMargin(): Int {
    val density = LocalDensity.current
    return remember(density) { with(density) { Motion.PredictiveBackMargin.roundToPx() } }
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

/** For anything that follows the back gesture as closely as the screen it drags away. */
fun <T> predictiveBackSpec(): FiniteAnimationSpec<T> =
    tween(PREDICTIVE_BACK_MILLIS, easing = Motion.PredictiveBack)

/**
 * The predictive back timeline itself, uneased: a value animated with this is how far the gesture
 * has gone, and once it's let go, how far the rest of the transition has played.
 */
fun <T> predictiveBackTimelineSpec(): FiniteAnimationSpec<T> =
    tween(PREDICTIVE_BACK_MILLIS, easing = LinearEasing)

/**
 * A screen the back gesture drags away, after Material's predictive back for full-screen surfaces:
 * it shrinks into a card that heads for the side the finger moves toward, stopping [edgeMargin]
 * short of the edge. [swipeEdge] is the [NavigationEvent] edge the gesture started from.
 *
 * It doesn't fade: the gesture can't tell the timeline when it's let go, so a fade here would
 * already be under way while the finger is still down. [PredictiveBackCard] fades the screen away
 * once the gesture is let go instead.
 */
fun predictiveBackOut(swipeEdge: Int, edgeMargin: Int): ExitTransition =
    scaleOut(predictiveBackSpec(), PREDICTIVE_BACK_SCALE) +
            slideOut(predictiveBackSpec()) { size ->
                // Into the room the shrinking leaves on that side, all but the margin
                val shift = max(0, (size.width * (1 - PREDICTIVE_BACK_SCALE) / 2).roundToInt() - edgeMargin)
                when (swipeEdge) {
                    NavigationEvent.EDGE_LEFT -> IntOffset(shift, 0)
                    NavigationEvent.EDGE_RIGHT -> IntOffset(-shift, 0)
                    else -> IntOffset.Zero
                }
            }

/**
 * A sheet ([sheetIn]) the back gesture drags away: it shrinks into a card like [predictiveBackOut]
 * and starts to sink, then drops out of sight once the gesture is let go.
 */
fun predictiveBackSheetOut(): ExitTransition =
    scaleOut(predictiveBackSpec(), PREDICTIVE_BACK_SCALE) +
            slideOutVertically(tween(PREDICTIVE_BACK_MILLIS, easing = Motion.EmphasizedAccelerate)) { it }

/**
 * The screen the back gesture uncovers. It slides in from the start side by [slideDistance], as
 * [sharedAxisXIn] does going back, and isn't dimmed: the card's shadow sets the two apart.
 */
fun predictiveBackIn(slideDistance: Int): EnterTransition =
    slideInHorizontally(predictiveBackSpec()) { -slideDistance }
