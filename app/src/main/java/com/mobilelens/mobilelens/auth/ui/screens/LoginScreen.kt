package com.mobilelens.mobilelens.auth.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.auth.ui.components.AuthErrorText
import com.mobilelens.mobilelens.auth.ui.components.AuthFormColumn
import com.mobilelens.mobilelens.auth.ui.components.AuthHeader
import com.mobilelens.mobilelens.auth.ui.components.AuthPrimaryButton
import com.mobilelens.mobilelens.auth.ui.components.AuthSecondaryButton
import com.mobilelens.mobilelens.auth.ui.components.AuthTextField
import com.mobilelens.mobilelens.auth.ui.components.ForgotPasswordDialog

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoginScreen(
    onLogin: (email: String, password: String) -> Unit,
    onNavigateToRegister: () -> Unit,
    errorMessage: String? = null,
    onClearError: () -> Unit = {},
    onRequestPasswordReset: (email: String) -> Unit = {},
    resetSending: Boolean = false,
    resetSent: Boolean = false,
    resetErrorMessage: String? = null,
    onClearPasswordReset: () -> Unit = {},
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showForgotPassword by rememberSaveable { mutableStateOf(false) }

    AuthFormColumn {
        AuthHeader(
            icon = Icons.AutoMirrored.Outlined.Login,
            title = stringResource(R.string.auth_log_in),
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Editing either field hides the error, since it describes the previous attempt
        AuthTextField(
            value = email,
            onValueChange = {
                email = it
                onClearError()
            },
            label = stringResource(R.string.auth_email),
            leadingIcon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
        )

        Spacer(modifier = Modifier.height(12.dp))

        AuthTextField(
            value = password,
            onValueChange = {
                password = it
                onClearError()
            },
            label = stringResource(R.string.auth_password),
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            imeAction = ImeAction.Done,
            onImeAction = { onLogin(email, password) },
        )

        AuthErrorText(message = errorMessage)

        TextButton(
            onClick = { showForgotPassword = true },
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier.align(Alignment.End),
        ) {
            Text(
                text = stringResource(R.string.auth_forgot_password),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        AuthPrimaryButton(
            text = stringResource(R.string.auth_log_in),
            onClick = { onLogin(email, password) },
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.auth_no_account),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(12.dp))

        AuthSecondaryButton(
            text = stringResource(R.string.auth_register_account),
            onClick = onNavigateToRegister,
        )
    }

    if (showForgotPassword) {
        ForgotPasswordDialog(
            initialEmail = email.trim(),
            sending = resetSending,
            sent = resetSent,
            errorMessage = resetErrorMessage,
            onSend = onRequestPasswordReset,
            onClearError = { if (resetErrorMessage != null) onClearPasswordReset() },
            onDismiss = {
                showForgotPassword = false
                onClearPasswordReset()
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen(
            onLogin = { _, _ -> },
            onNavigateToRegister = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenErrorPreview() {
    MaterialTheme {
        LoginScreen(
            onLogin = { _, _ -> },
            onNavigateToRegister = {},
            errorMessage = stringResource(R.string.auth_error_invalid_credentials)
        )
    }
}
