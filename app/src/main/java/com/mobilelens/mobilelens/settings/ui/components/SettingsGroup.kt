package com.mobilelens.mobilelens.settings.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Segmented list items that belong together, stacked with the small gap that lets their shapes
 * read as one group. Each item takes its shape from `ListItemDefaults.segmentedShapes`.
 */
@Composable
fun SettingsGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        content = content,
    )
}

/**
 * Colors for a settings group's items. The default segmented container is the surface color,
 * which this theme also uses as the background, so the items would have no visible shape. A
 * checked item keeps the same container, as its switch already shows the state.
 */
@Composable
fun settingsItemColors(
    contentColor: Color = Color.Unspecified,
    leadingContentColor: Color = Color.Unspecified,
): ListItemColors {
    val container = MaterialTheme.colorScheme.surfaceContainer
    return ListItemDefaults.segmentedColors(
        containerColor = container,
        contentColor = contentColor,
        leadingContentColor = leadingContentColor,
        selectedContainerColor = container,
    )
}
