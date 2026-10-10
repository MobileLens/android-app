package com.mobilelens.mobilelens.core.ui

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.ui.theme.fadeThroughIn
import com.mobilelens.mobilelens.core.ui.theme.fadeThroughOut

// Full-screen placeholders shown by routes while their data is loading or failed to load

@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorContent(
    @StringRes messageRes: Int,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(messageRes), modifier = Modifier.padding(16.dp))
            if (onRetry != null) {
                TextButton(onClick = onRetry) {
                    Text(text = stringResource(R.string.action_retry))
                }
            }
        }
    }
}

/**
 * Fades between a UI state's Loading, Error and Success content. Only a change to another kind of
 * state animates, as told apart by [contentKey]: a new state of the same kind updates the content
 * in place, so its scroll position and other remembered state survive.
 */
@Composable
fun <S : Any> UiStateCrossfade(
    state: S,
    modifier: Modifier = Modifier,
    contentKey: (S) -> Any = { it::class },
    content: @Composable (S) -> Unit,
) {
    AnimatedContent(
        targetState = state,
        modifier = modifier,
        transitionSpec = { fadeThroughIn() togetherWith fadeThroughOut() },
        contentKey = contentKey,
        label = "UiStateCrossfade",
    ) { shownState ->
        content(shownState)
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingContentPreview() {
    MaterialTheme {
        LoadingContent()
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorContentPreview() {
    MaterialTheme {
        ErrorContent(messageRes = R.string.error_load_phone)
    }
}
