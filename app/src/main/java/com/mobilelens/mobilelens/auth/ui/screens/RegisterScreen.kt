package com.mobilelens.mobilelens.auth.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.auth.data.isStrongPassword
import com.mobilelens.mobilelens.auth.ui.components.AuthErrorText
import com.mobilelens.mobilelens.auth.ui.components.AuthFormColumn
import com.mobilelens.mobilelens.auth.ui.components.AuthHeader
import com.mobilelens.mobilelens.auth.ui.components.AuthPrimaryButton
import com.mobilelens.mobilelens.auth.ui.components.AuthTextField

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RegisterScreen(
    onRegister: (username: String, email: String, password: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    errorMessage: String? = null,
    onClearError: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var passwordsDiffer by remember { mutableStateOf(false) }

    fun submit() {
        if (password == repeatPassword) {
            onRegister(username, email, password)
        } else {
            passwordsDiffer = true
        }
    }

    AuthFormColumn {
        AuthHeader(
            icon = Icons.Outlined.PersonAdd,
            title = stringResource(R.string.auth_register),
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Editing any field hides the error, since it describes the previous attempt
        AuthTextField(
            value = username,
            onValueChange = {
                username = it
                onClearError()
            },
            label = stringResource(R.string.auth_username),
            leadingIcon = Icons.Outlined.Person,
            imeAction = ImeAction.Next,
        )

        Spacer(modifier = Modifier.height(12.dp))

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
                passwordsDiffer = false
                onClearError()
            },
            label = stringResource(R.string.auth_password),
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            imeAction = ImeAction.Next,
        )

        // The rule only shows once there's a password that breaks it
        AnimatedVisibility(
            visible = password.isNotEmpty() && !isStrongPassword(password),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Text(
                text = stringResource(R.string.settings_password_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp, end = 16.dp),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AuthTextField(
            value = repeatPassword,
            onValueChange = {
                repeatPassword = it
                passwordsDiffer = false
                onClearError()
            },
            label = stringResource(R.string.auth_repeat_password),
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            imeAction = ImeAction.Done,
            onImeAction = ::submit,
        )

        val shownError = if (passwordsDiffer) {
            stringResource(R.string.auth_error_passwords_differ)
        } else {
            errorMessage
        }
        AuthErrorText(message = shownError)

        Spacer(modifier = Modifier.height(32.dp))

        AuthPrimaryButton(
            text = stringResource(R.string.auth_register),
            onClick = ::submit,
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToLogin, shapes = ButtonDefaults.shapes()) {
            Text(
                text = stringResource(R.string.auth_have_account),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreenPreview() {
    MaterialTheme {
        RegisterScreen(
            onRegister = { _, _, _ -> },
            onNavigateToLogin = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreenErrorPreview() {
    MaterialTheme {
        RegisterScreen(
            onRegister = { _, _, _ -> },
            onNavigateToLogin = {},
            errorMessage = "Password must have at least 8 characters."
        )
    }
}
