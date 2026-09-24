package com.mobilelens.mobilelens.auth.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.auth.ui.components.AuthHeaderTitle
import com.mobilelens.mobilelens.auth.ui.components.AuthPillButton
import com.mobilelens.mobilelens.auth.ui.components.AuthTextField

@Composable
fun RegisterScreen(
    onRegister: (username: String, email: String, password: String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AuthHeaderTitle(text = "Register")

        Spacer(modifier = Modifier.height(32.dp))

        AuthTextField(
            value = username,
            onValueChange = { username = it },
            label = "Username"
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "E-mail"
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            isPassword = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = repeatPassword,
            onValueChange = { repeatPassword = it },
            label = "Repeat password",
            isPassword = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        AuthPillButton(
            text = "Register",
            onClick = { onRegister(username, email, password) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Already have an account? Log in",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable { onNavigateToLogin() }
        )
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
