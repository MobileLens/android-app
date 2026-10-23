package com.mobilelens.mobilelens.core.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.mobilelens.mobilelens.R

/** One labelled action in a [FabMenu]. */
data class FabMenuItem(
    val icon: ImageVector,
    @StringRes val labelRes: Int,
    val onClick: () -> Unit,
)

/**
 * A toggle FAB that opens [items] in a Material 3 Expressive speed-dial menu above it. The actions
 * are spaced and animated by the menu itself, and the + morphs into a × while the menu is open.
 * Choosing an action closes the menu, and the back gesture closes it without choosing anything.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FabMenu(
    items: List<FabMenuItem>,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    BackHandler(expanded) { expanded = false }

    val menuDescription = stringResource(
        if (expanded) R.string.common_close_menu else R.string.common_open_menu
    )
    FloatingActionButtonMenu(
        expanded = expanded,
        button = {
            ToggleFloatingActionButton(
                modifier = Modifier.semantics { contentDescription = menuDescription },
                checked = expanded,
                onCheckedChange = {
                    haptic.performToggle(!expanded)
                    expanded = !expanded
                },
            ) {
                // Past halfway the icon is already the ×, so it swaps while it morphs
                val imageVector by remember {
                    derivedStateOf { if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add }
                }
                Icon(
                    painter = rememberVectorPainter(imageVector),
                    contentDescription = null,
                    modifier = with(ToggleFloatingActionButtonDefaults) {
                        Modifier.animateIcon({ checkedProgress })
                    },
                )
            }
        },
        modifier = modifier,
    ) {
        items.forEach { item ->
            FloatingActionButtonMenuItem(
                onClick = {
                    expanded = false
                    item.onClick()
                },
                icon = { Icon(item.icon, contentDescription = null) },
                text = { Text(stringResource(item.labelRes)) },
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
