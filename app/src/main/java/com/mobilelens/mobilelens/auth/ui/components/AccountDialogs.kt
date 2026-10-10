package com.mobilelens.mobilelens.auth.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.ui.LoadingButtonContent

// The account screen's dialogs. While [saving] they can't be dismissed and their fields are locked,
// so the result always has somewhere to show; [errorMessage] is the last attempt's failure, and
// editing a field drops it through onClearError.

@Composable
fun ChangeUsernameDialog(
    currentUsername: String,
    saving: Boolean,
    errorMessage: String?,
    onSave: (String) -> Unit,
    onClearError: () -> Unit,
    onDismiss: () -> Unit,
) {
    var username by rememberSaveable { mutableStateOf(currentUsername) }
    val newUsername = username.trim()

    AccountDialog(
        icon = Icons.Outlined.Person,
        title = stringResource(R.string.settings_change_username_title),
        confirmLabel = stringResource(R.string.common_save),
        canConfirm = newUsername.isNotEmpty() && newUsername != currentUsername,
        saving = saving,
        errorMessage = errorMessage,
        onConfirm = { onSave(newUsername) },
        onDismiss = onDismiss,
    ) {
        AuthTextField(
            value = username,
            onValueChange = {
                username = it
                onClearError()
            },
            label = stringResource(R.string.settings_new_username),
            enabled = !saving,
        )
    }
}

@Composable
fun ChangeEmailDialog(
    currentEmail: String,
    saving: Boolean,
    errorMessage: String?,
    onSave: (String) -> Unit,
    onClearError: () -> Unit,
    onDismiss: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf(currentEmail) }
    val newEmail = email.trim()

    AccountDialog(
        icon = Icons.Outlined.Email,
        title = stringResource(R.string.settings_change_email_title),
        confirmLabel = stringResource(R.string.common_save),
        // The backend refuses the address the account already has
        canConfirm = newEmail.isNotEmpty() && !newEmail.equals(currentEmail, ignoreCase = true),
        saving = saving,
        errorMessage = errorMessage,
        onConfirm = { onSave(newEmail) },
        onDismiss = onDismiss,
    ) {
        DialogSupportingText(stringResource(R.string.settings_change_email_supporting))
        AuthTextField(
            value = email,
            onValueChange = {
                email = it
                onClearError()
            },
            label = stringResource(R.string.settings_new_email),
            keyboardType = KeyboardType.Email,
            enabled = !saving,
        )
    }
}

@Composable
fun ChangePasswordDialog(
    saving: Boolean,
    errorMessage: String?,
    onSave: (currentPassword: String, newPassword: String) -> Unit,
    onClearError: () -> Unit,
    onDismiss: () -> Unit,
) {
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var repeatedPassword by rememberSaveable { mutableStateOf("") }
    // Checked here, since only this dialog knows the repeated password
    var passwordsDiffer by rememberSaveable { mutableStateOf(false) }

    fun edited() {
        passwordsDiffer = false
        onClearError()
    }

    AccountDialog(
        icon = Icons.Outlined.Lock,
        title = stringResource(R.string.settings_change_password_title),
        confirmLabel = stringResource(R.string.common_save),
        canConfirm = currentPassword.isNotEmpty() && newPassword.isNotEmpty() && repeatedPassword.isNotEmpty(),
        saving = saving,
        errorMessage = if (passwordsDiffer) stringResource(R.string.auth_error_passwords_differ) else errorMessage,
        onConfirm = {
            if (newPassword != repeatedPassword) {
                passwordsDiffer = true
            } else {
                onSave(currentPassword, newPassword)
            }
        },
        onDismiss = onDismiss,
    ) {
        DialogSupportingText(stringResource(R.string.settings_change_password_supporting))
        AuthTextField(
            value = currentPassword,
            onValueChange = {
                currentPassword = it
                edited()
            },
            label = stringResource(R.string.settings_current_password),
            isPassword = true,
            enabled = !saving,
        )
        Spacer(modifier = Modifier.height(8.dp))
        AuthTextField(
            value = newPassword,
            onValueChange = {
                newPassword = it
                edited()
            },
            label = stringResource(R.string.settings_new_password),
            isPassword = true,
            enabled = !saving,
        )
        Spacer(modifier = Modifier.height(8.dp))
        AuthTextField(
            value = repeatedPassword,
            onValueChange = {
                repeatedPassword = it
                edited()
            },
            label = stringResource(R.string.settings_repeat_new_password),
            isPassword = true,
            enabled = !saving,
        )
        Text(
            text = stringResource(R.string.settings_password_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
fun DeleteAccountDialog(
    deleting: Boolean,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AccountDialog(
        icon = Icons.Outlined.DeleteForever,
        title = stringResource(R.string.settings_delete_account_title),
        confirmLabel = stringResource(R.string.common_delete),
        canConfirm = true,
        saving = deleting,
        errorMessage = errorMessage,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        destructive = true,
    ) {
        Text(stringResource(R.string.settings_delete_account_message))
    }
}

/** Confirms deleting one of the user's reviews; the list shows the deletion's progress. */
@Composable
fun DeleteReviewDialog(
    reviewTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AccountDialog(
        icon = Icons.Outlined.Delete,
        title = stringResource(R.string.settings_delete_review_title),
        confirmLabel = stringResource(R.string.common_delete),
        canConfirm = true,
        saving = false,
        errorMessage = null,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        destructive = true,
    ) {
        Text(stringResource(R.string.settings_delete_review_message, reviewTitle))
    }
}

@Composable
private fun AccountDialog(
    icon: ImageVector,
    title: String,
    confirmLabel: String,
    canConfirm: Boolean,
    saving: Boolean,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        icon = { Icon(imageVector = icon, contentDescription = null) },
        title = { Text(title) },
        text = {
            Column {
                content()
                AuthErrorText(message = errorMessage)
            }
        },
        confirmButton = {
            TextButton(
                // Stays enabled while saving, so its spinner isn't greyed out
                onClick = { if (!saving) onConfirm() },
                enabled = canConfirm,
                colors = if (destructive) {
                    ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                } else {
                    ButtonDefaults.textButtonColors()
                },
            ) {
                LoadingButtonContent(text = confirmLabel, loading = saving)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !saving) {
                Text(stringResource(R.string.common_cancel))
            }
        },
    )
}

@Composable
private fun DialogSupportingText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(bottom = 16.dp),
    )
}

@Preview
@Composable
private fun ChangePasswordDialogPreview() {
    MaterialTheme {
        ChangePasswordDialog(
            saving = false,
            errorMessage = stringResource(R.string.settings_error_wrong_password),
            onSave = { _, _ -> },
            onClearError = {},
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun DeleteAccountDialogPreview() {
    MaterialTheme {
        DeleteAccountDialog(deleting = true, errorMessage = null, onConfirm = {}, onDismiss = {})
    }
}
