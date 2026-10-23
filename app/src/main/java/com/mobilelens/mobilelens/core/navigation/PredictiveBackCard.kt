package com.mobilelens.mobilelens.core.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.mobilelens.mobilelens.core.ui.theme.Motion
import com.mobilelens.mobilelens.core.ui.theme.predictiveBackSpec
import com.mobilelens.mobilelens.core.ui.theme.predictiveBackTimelineSpec

/**
 * Adds [T]'s screen to the graph, as [composable] does, drawn as a [PredictiveBackCard]. Every
 * screen goes through this, so the back gesture can drag any of them away.
 */
inline fun <reified T : Any> NavGraphBuilder.screen(
    navController: NavController,
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable<T> { entry ->
        PredictiveBackCard(navController, entry) { content(entry) }
    }
}

/**
 * Screens have no background of their own, so while the back gesture shrinks one away (see
 * [NavTransitions]) it gets the app's background, rounded corners and a shadow. The screen it
 * uncovers then shows around it rather than through it.
 *
 * The gesture scrubs through the transition, so the transition can't tell when it's let go. This
 * does: from then on the card carries on the way it was heading, off the screen, fading as it goes,
 * over what's left of the transition. A sheet just sinks away.
 */
@Composable
fun AnimatedVisibilityScope.PredictiveBackCard(
    navController: NavController,
    entry: NavBackStackEntry,
    content: @Composable () -> Unit,
) {
    val leaving = transition.targetState == EnterExitState.PostExit
    // Only the back gesture moves a screen out while it's still the current one; a pop or a new
    // screen has already changed the back stack. Once the gesture is let go and the screen is
    // popped, it stays a card until it's gone.
    val swipedAway = remember(leaving) {
        leaving && navController.currentBackStackEntry?.id == entry.id
    }
    val currentEntry by navController.currentBackStackEntryAsState()
    val released = swipedAway && currentEntry?.id != entry.id
    val flungAway = remember(entry) { !entry.destination.isSheet() }
    // The card heads away from the edge the gesture started from
    val dispatcher = LocalNavigationEventDispatcherOwner.current?.navigationEventDispatcher
    val direction = remember(swipedAway) {
        val gesture = dispatcher?.transitionState?.value as? NavigationEventTransitionState.InProgress
        when (gesture?.latestEvent?.swipeEdge) {
            NavigationEvent.EDGE_LEFT -> 1f
            NavigationEvent.EDGE_RIGHT -> -1f
            else -> 0f
        }
    }

    // How far the card has formed: corners and shadow follow the gesture as the shrinking does
    val lift = transition.animateFloat(
        transitionSpec = { if (swipedAway) predictiveBackSpec() else snap() },
        label = "predictiveBackLift",
    ) { state -> if (state == EnterExitState.PostExit) 1f else 0f }
    // Where the transition is, to time what happens after the gesture is let go
    val timeline = transition.animateFloat(
        transitionSpec = { if (swipedAway) predictiveBackTimelineSpec() else snap() },
        label = "predictiveBackTimeline",
    ) { state -> if (state == EnterExitState.PostExit) 1f else 0f }
    // Read without observing, so the card isn't recomposed on every frame of the transition
    val releasedAt = remember(released) { Snapshot.withoutReadObservation { timeline.value } }
    val background = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (swipedAway) {
                    Modifier
                        .graphicsLayer {
                            if (released && flungAway) {
                                val rest = 1f - releasedAt
                                val progress = if (rest > 0f) {
                                    ((timeline.value - releasedAt) / rest).coerceIn(0f, 1f)
                                } else {
                                    1f
                                }
                                translationX = direction * size.width * Motion.Emphasized.transform(progress)
                                alpha = 1f - progress
                            }
                            shape = RoundedCornerShape(Motion.PredictiveBackCornerRadius * lift.value)
                            shadowElevation = (Motion.PredictiveBackElevation * lift.value).toPx()
                            clip = true
                        }
                        .background(background)
                } else {
                    Modifier
                }
            ),
    ) {
        content()
    }
}
