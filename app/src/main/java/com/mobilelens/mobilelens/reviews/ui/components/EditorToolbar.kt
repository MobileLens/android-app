package com.mobilelens.mobilelens.reviews.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R

@Composable
fun EditorToolbar(
    modifier: Modifier = Modifier,
    bold: Boolean = false,
    italic: Boolean = false,
    underline: Boolean = false,
    onBoldClick: () -> Unit = {},
    onItalicClick: () -> Unit = {},
    onUnderlineClick: () -> Unit = {},
    onImageClick: () -> Unit = {},
    onUndoClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier
            .wrapContentWidth()
            .height(64.dp),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 6.dp,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            EditorToolbarButton(
                selected = bold,
                onClick = onBoldClick,
            ) {
                val boldDescription = stringResource(R.string.editor_bold)
                Text(
                    text = stringResource(R.string.editor_bold_glyph),
                    // Read the action name instead of the single letter
                    modifier = Modifier.semantics { contentDescription = boldDescription },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }

            EditorToolbarButton(
                selected = italic,
                onClick = onItalicClick,
            ) {
                val italicDescription = stringResource(R.string.editor_italic)
                Text(
                    text = stringResource(R.string.editor_italic_glyph),
                    // Read the action name instead of the single letter
                    modifier = Modifier.semantics { contentDescription = italicDescription },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }

            EditorToolbarButton(
                selected = underline,
                onClick = onUnderlineClick,
            ) {
                val underlineDescription = stringResource(R.string.editor_underline)
                Text(
                    text = stringResource(R.string.editor_underline_glyph),
                    // Read the action name instead of the single letter
                    modifier = Modifier.semantics { contentDescription = underlineDescription },
                    style = MaterialTheme.typography.titleLarge.copy(
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }

            EditorToolbarButton(onClick = onImageClick) {
                Icon(
                    imageVector = Icons.Rounded.Image,
                    contentDescription = stringResource(R.string.editor_insert_image),
                )
            }

            EditorToolbarButton(onClick = onUndoClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Undo,
                    contentDescription = stringResource(R.string.editor_undo),
                )
            }
        }
    }
}

@Composable
private fun EditorToolbarButton(
    selected: Boolean = false,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val selectedColor = MaterialTheme.colorScheme.secondaryContainer
    val containerColor by animateColorAsState(
        // A see-through copy of the selected color, so it doesn't pass through grey on the way
        targetValue = if (selected) selectedColor else selectedColor.copy(alpha = 0f),
        label = "EditorToolbarButtonContainer",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "EditorToolbarButtonContent",
    )
    Surface(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        shape = RoundedCornerShape(24.dp),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Box(
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Preview
@Composable
fun EditorToolbarPreview() {
    EditorToolbar(
        bold = true,
        italic = true,
        underline = true,
    )
}
