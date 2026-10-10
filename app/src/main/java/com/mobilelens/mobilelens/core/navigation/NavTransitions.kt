package com.mobilelens.mobilelens.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import com.mobilelens.mobilelens.core.ui.theme.fadeThroughIn
import com.mobilelens.mobilelens.core.ui.theme.fadeThroughOut
import com.mobilelens.mobilelens.core.ui.theme.rememberSlideDistance
import com.mobilelens.mobilelens.core.ui.theme.sharedAxisXIn
import com.mobilelens.mobilelens.core.ui.theme.sharedAxisXOut
import com.mobilelens.mobilelens.core.ui.theme.sheetIn
import com.mobilelens.mobilelens.core.ui.theme.sheetOut

// Tabs scale up slightly as they fade in, as Material's fade through does
private const val TAB_INITIAL_SCALE = 0.92f

private typealias EnterSpec = AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition
private typealias ExitSpec = AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition
// Also given the edge the back gesture started from
private typealias PredictiveEnterSpec = AnimatedContentTransitionScope<NavBackStackEntry>.(Int) -> EnterTransition
private typealias PredictiveExitSpec = AnimatedContentTransitionScope<NavBackStackEntry>.(Int) -> ExitTransition

/**
 * Transitions between the NavHost's screens, after Material's navigation motion:
 * - switching between bottom-bar tabs fades through, since the tabs aren't related to each other;
 * - opening any other screen slides it in from the end side, and going back slides the other way;
 * - sheets ([isSheet]) rise from the bottom over the screen that opened them and sink back down.
 *
 * Screens have no background of their own, so the slides cross-fade rather than overlap; sheets
 * draw their own. The predictive back gesture scrubs through the same pop transitions.
 */
class NavTransitions(private val slideDistance: Int) {
    val enter: EnterSpec = {
        when {
            targetState.destination.isSheet() -> sheetIn()
            targetState.destination.isTopLevel() -> fadeThroughIn(TAB_INITIAL_SCALE)
            else -> sharedAxisXIn(forward = true, slideDistance)
        }
    }

    val exit: ExitSpec = {
        when {
            // Stays put until the sheet has covered it
            targetState.destination.isSheet() -> ExitTransition.KeepUntilTransitionsFinished
            targetState.destination.isTopLevel() -> fadeThroughOut()
            else -> sharedAxisXOut(forward = true, slideDistance)
        }
    }

    val popEnter: EnterSpec = {
        when {
            // Was under the sheet the whole time
            initialState.destination.isSheet() -> EnterTransition.None
            isBetweenTabs() -> fadeThroughIn(TAB_INITIAL_SCALE)
            else -> sharedAxisXIn(forward = false, slideDistance)
        }
    }

    val popExit: ExitSpec = {
        when {
            initialState.destination.isSheet() -> sheetOut()
            isBetweenTabs() -> fadeThroughOut()
            else -> sharedAxisXOut(forward = false, slideDistance)
        }
    }

    // Navigation's own predictive back shrinks the old screen without fading it, which would leave
    // it drawn over the screen it uncovers
    val predictivePopEnter: PredictiveEnterSpec = { popEnter(this) }
    val predictivePopExit: PredictiveExitSpec = { popExit(this) }

    // Pressing back on a tab, or the Home tab, pops to Home
    private fun AnimatedContentTransitionScope<NavBackStackEntry>.isBetweenTabs(): Boolean =
        initialState.destination.isTopLevel() && targetState.destination.isTopLevel()
}

@Composable
fun rememberNavTransitions(): NavTransitions {
    val slideDistance = rememberSlideDistance()
    return remember(slideDistance) { NavTransitions(slideDistance) }
}
