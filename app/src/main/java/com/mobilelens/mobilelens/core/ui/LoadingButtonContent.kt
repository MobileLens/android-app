package com.mobilelens.mobilelens.core.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.ui.theme.fadeThroughIn
import com.mobilelens.mobilelens.core.ui.theme.fadeThroughOut

/**
 * A button's [text], swapped for a small spinner while [loading]. The two fade through each other
 * and the button eases between their widths.
 */
@Composable
fun LoadingButtonContent(
    text: String,
    loading: Boolean,
) {
    AnimatedContent(
        targetState = loading,
        contentAlignment = Alignment.Center,
        transitionSpec = { fadeThroughIn() togetherWith fadeThroughOut() },
        label = "LoadingButtonContent",
    ) { isLoading ->
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Text(text)
        }
    }
}

@Preview
@Composable
private fun LoadingButtonContentPreview() {
    MaterialTheme {
        Button(onClick = {}) {
            LoadingButtonContent(text = stringResource(R.string.review_editor_publish), loading = true)
        }
    }
}
