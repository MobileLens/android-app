package com.mobilelens.mobilelens.auth.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.mobilelens.mobilelens.R

/**
 * Asks for the address to send a password reset link to, starting from [initialEmail]. Once [sent],
 * it confirms the request instead; the backend doesn't say whether the address has an account.
 */
@Composable
fun ForgotPasswordDialog(
    initialEmail: String,
    sending: Boolean,
    sent: Boolean,
    errorMessage: String?,
    onSend: (String) -> Unit,
    onClearError: () -> Unit,
    onDismiss: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf(initialEmail) }
    val trimmedEmail = email.trim()

    if (sent) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = { Icon(imageVector = Icons.Outlined.MarkEmailRead, contentDescription = null) },
            title = { Text(stringResource(R.string.auth_reset_sent_title)) },
            text = { Text(stringResource(R.string.auth_reset_sent_message, trimmedEmail)) },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.auth_reset_done))
                }
            },
        )
        return
    }

    AccountDialog(
        icon = Icons.Outlined.LockReset,
        title = stringResource(R.string.auth_reset_title),
        confirmLabel = stringResource(R.string.auth_reset_send),
        canConfirm = trimmedEmail.isNotEmpty(),
        saving = sending,
        errorMessage = errorMessage,
        onConfirm = { onSend(trimmedEmail) },
        onDismiss = onDismiss,
    ) {
        DialogSupportingText(stringResource(R.string.auth_reset_supporting))
        AuthTextField(
            value = email,
            onValueChange = {
                email = it
                onClearError()
            },
            label = stringResource(R.string.auth_email),
            leadingIcon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Send,
            onImeAction = { if (trimmedEmail.isNotEmpty() && !sending) onSend(trimmedEmail) },
            enabled = !sending,
        )
    }
}

@Preview
@Composable
private fun ForgotPasswordDialogPreview() {
    MaterialTheme {
        ForgotPasswordDialog(
            initialEmail = "jan@example.com",
            sending = false,
            sent = false,
            errorMessage = null,
            onSend = {},
            onClearError = {},
            onDismiss = {},
        )
    }
}
