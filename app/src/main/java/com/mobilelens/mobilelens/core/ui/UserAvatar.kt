package com.mobilelens.mobilelens.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A circle with the user's [initial], or a generic person icon when it's null (no one signed in).
 * [textStyle] should suit [size].
 */
@Composable
fun UserAvatar(
    initial: String?,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (initial != null) {
            Text(
                text = initial,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = textStyle,
                fontWeight = FontWeight.Bold
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(size * 0.625f)
            )
        }
    }
}

@Preview
@Composable
private fun UserAvatarPreview() {
    MaterialTheme {
        Row {
            UserAvatar(initial = "M")
            UserAvatar(initial = null)
            UserAvatar(initial = "M", size = 88.dp, textStyle = MaterialTheme.typography.displaySmall)
        }
    }
}
