package com.mobilelens.mobilelens.core.ui

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.ui.theme.Motion

// Delay between one action appearing and the next one up
private const val ITEM_STAGGER_MILLIS = 40

// Actions grow out of the FAB's corner as they appear
private const val ITEM_INITIAL_SCALE = 0.8f
private val ItemTransformOrigin = TransformOrigin(1f, 1f)

/** One labelled action in a [FabMenu]. */
data class FabMenuItem(
    val icon: ImageVector,
    @StringRes val labelRes: Int,
    val onClick: () -> Unit,
)

/**
 * A FAB that opens [items] in a column above it. The actions rise one after another, starting
 * with the one next to the FAB, and the + spins into a × while the menu is open. Choosing an
 * action closes the menu.
 */
@Composable
fun FabMenu(
    items: List<FabMenuItem>,
    modifier: Modifier = Modifier,
    itemSpacing: Dp = 12.dp,
) {
    var expanded by remember { mutableStateOf(false) }
    // The + turned by 45° is the ×
    val iconRotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "FabMenuIconRotation",
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(itemSpacing),
    ) {
        items.forEachIndexed { index, item ->
            val delayMillis = (items.lastIndex - index) * ITEM_STAGGER_MILLIS
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(Motion.DURATION_SHORT, delayMillis)) +
                        slideInVertically(
                            tween(Motion.DURATION_MEDIUM, delayMillis, Motion.EmphasizedDecelerate)
                        ) { it / 2 } +
                        scaleIn(
                            tween(Motion.DURATION_MEDIUM, delayMillis, Motion.EmphasizedDecelerate),
                            initialScale = ITEM_INITIAL_SCALE,
                            transformOrigin = ItemTransformOrigin,
                        ),
                // All at once, so the menu is out of the way quickly
                exit = fadeOut(tween(Motion.DURATION_SHORT, easing = Motion.EmphasizedAccelerate)) +
                        slideOutVertically(
                            tween(Motion.DURATION_SHORT, easing = Motion.EmphasizedAccelerate)
                        ) { it / 2 } +
                        scaleOut(
                            tween(Motion.DURATION_SHORT, easing = Motion.EmphasizedAccelerate),
                            targetScale = ITEM_INITIAL_SCALE,
                            transformOrigin = ItemTransformOrigin,
                        ),
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        expanded = false
                        item.onClick()
                    },
                    icon = { Icon(item.icon, contentDescription = null) },
                    text = { Text(stringResource(item.labelRes)) },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }

        val menuDescription = stringResource(
            if (expanded) R.string.common_close_menu else R.string.common_open_menu
        )
        FloatingActionButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.semantics {
                contentDescription = menuDescription
            },
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.rotate(iconRotation),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FabMenuPreview() {
    MaterialTheme {
        FabMenu(
            items = listOf(
                FabMenuItem(Icons.Filled.RateReview, R.string.action_write_review) {},
                FabMenuItem(Icons.AutoMirrored.Filled.CompareArrows, R.string.action_compare) {},
            ),
        )
    }
}
