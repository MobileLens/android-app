package com.mobilelens.mobilelens.core.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateDp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mobilelens.mobilelens.core.ui.theme.Motion
import com.mobilelens.mobilelens.core.ui.theme.predictiveBackSpec

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
 * [NavTransitions]) it gets the app's background and rounded corners. The screen it uncovers then
 * shows around it rather than through it.
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
    val cornerRadius by transition.animateDp(
        transitionSpec = { predictiveBackSpec() },
        label = "predictiveBackCornerRadius",
    ) { state ->
        if (state == EnterExitState.PostExit) Motion.PredictiveBackCornerRadius else 0.dp
    }
    val background = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (swipedAway) {
                    Modifier
                        .graphicsLayer {
                            shape = RoundedCornerShape(cornerRadius)
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
