package com.mobilelens.mobilelens.auth.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.ui.theme.fadeThroughIn
import com.mobilelens.mobilelens.core.ui.theme.fadeThroughOut

/**
 * An error under a form, or nothing when [message] is null. It fades in while the form makes room
 * for it, and keeps showing the old text as it fades back out.
 */
@Composable
fun AuthErrorText(
    message: String?,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = message,
        modifier = modifier.fillMaxWidth(),
        // Unclipped, so the text isn't cut off while the space for it grows
        transitionSpec = { fadeThroughIn() togetherWith fadeThroughOut() using SizeTransform(clip = false) },
        label = "AuthErrorText",
    ) { shownMessage ->
        if (shownMessage != null) {
            Text(
                text = shownMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthErrorTextPreview() {
    MaterialTheme {
        AuthErrorText(message = stringResource(R.string.auth_error_invalid_credentials))
    }
}
