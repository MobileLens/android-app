package com.mobilelens.mobilelens.auth.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R

// The auth forms' buttons are the medium Expressive size, full width, so the main action is easy to hit
private val AuthButtonHeight = ButtonDefaults.MediumContainerHeight

/** The form's main action, e.g. Log in or Register. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        shapes = ButtonDefaults.shapesFor(AuthButtonHeight),
        contentPadding = ButtonDefaults.contentPaddingFor(AuthButtonHeight),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = AuthButtonHeight),
    ) {
        AuthButtonText(text)
    }
}

/** A way out of the form that's still a full action, e.g. switching from Log in to Register. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AuthSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        shapes = ButtonDefaults.shapesFor(AuthButtonHeight),
        contentPadding = ButtonDefaults.contentPaddingFor(AuthButtonHeight),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = AuthButtonHeight),
    ) {
        AuthButtonText(text)
    }
}

@Composable
private fun AuthButtonText(text: String) {
    Text(
        text = text,
        style = ButtonDefaults.textStyleFor(AuthButtonHeight),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Preview(showBackground = true)
@Composable
private fun AuthButtonsPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AuthPrimaryButton(text = stringResource(R.string.auth_log_in), onClick = {})
            AuthSecondaryButton(
                text = stringResource(R.string.auth_register_account),
                onClick = {},
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}
