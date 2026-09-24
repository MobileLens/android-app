package com.mobilelens.mobilelens.auth.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobilelens.mobilelens.auth.model.User
import com.mobilelens.mobilelens.auth.ui.components.AuthPillButton
import com.mobilelens.mobilelens.auth.ui.components.AuthTextField
import com.mobilelens.mobilelens.auth.ui.components.DangerRedColor
import com.mobilelens.mobilelens.auth.ui.components.PersonalInfoRow
import com.mobilelens.mobilelens.auth.ui.components.UserReviewHistoryCard
import com.mobilelens.mobilelens.reviews.model.Review

@Composable
fun UserSettingsScreen(
    user: User,
    userReviews: List<Review>,
    onBackClick: () -> Unit,
    onUpdateUsername: (String) -> Unit,
    onUpdateEmail: (String) -> Unit,
    onDeleteAccount: () -> Unit,
    onLogout: () -> Unit,
    onDeleteReview: (String) -> Unit = {}
) {
    var showChangeUsernameDialog by remember { mutableStateOf(false) }
    var showChangeEmailDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    var newUsernameInput by remember { mutableStateOf(user.username) }
    var newEmailInput by remember { mutableStateOf(user.email) }
    var newPasswordInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Text(
            text = "${user.username}'s settings",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Section 1: Personal info
        Text(
            text = "Personal info",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        PersonalInfoRow(label = "Username", value = user.username)
        PersonalInfoRow(label = "E-mail", value = user.email)
        PersonalInfoRow(label = "Role", value = user.role)

        Spacer(modifier = Modifier.height(28.dp))

        // Section 2: Change
        Text(
            text = "Change",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AuthPillButton(
                text = "Username",
                onClick = {
                    newUsernameInput = user.username
                    showChangeUsernameDialog = true
                },
                modifier = Modifier.weight(1f)
            )

            AuthPillButton(
                text = "E-mail",
                onClick = {
                    newEmailInput = user.email
                    showChangeEmailDialog = true
                },
                modifier = Modifier.weight(1f)
            )

            AuthPillButton(
                text = "Password",
                onClick = {
                    newPasswordInput = ""
                    showChangePasswordDialog = true
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Section 3: Danger zone
        Text(
            text = "Danger zone",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { showDeleteAccountDialog = true },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DangerRedColor)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete account", color = Color.White)
            }

            Spacer(modifier = Modifier.width(12.dp))

            TextButton(
                onClick = onLogout,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text("Log out", color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Section 4: Review history
        Text(
            text = "Review history",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (userReviews.isEmpty()) {
            Text(
                text = "No review history yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            userReviews.forEach { review ->
                UserReviewHistoryCard(
                    review = review,
                    onDeleteReview = onDeleteReview
                )
            }
        }
    }

    // Dialogs
    if (showChangeUsernameDialog) {
        AlertDialog(
            onDismissRequest = { showChangeUsernameDialog = false },
            title = { Text("Change Username") },
            text = {
                AuthTextField(
                    value = newUsernameInput,
                    onValueChange = { newUsernameInput = it },
                    label = "New Username"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newUsernameInput.isNotBlank()) {
                            onUpdateUsername(newUsernameInput)
                        }
                        showChangeUsernameDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangeUsernameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showChangeEmailDialog) {
        AlertDialog(
            onDismissRequest = { showChangeEmailDialog = false },
            title = { Text("Change E-mail") },
            text = {
                AuthTextField(
                    value = newEmailInput,
                    onValueChange = { newEmailInput = it },
                    label = "New E-mail"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newEmailInput.isNotBlank()) {
                            onUpdateEmail(newEmailInput)
                        }
                        showChangeEmailDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangeEmailDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Change Password") },
            text = {
                AuthTextField(
                    value = newPasswordInput,
                    onValueChange = { newPasswordInput = it },
                    label = "New Password",
                    isPassword = true
                )
            },
            confirmButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to delete your account? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        onDeleteAccount()
                    }
                ) {
                    Text("Delete", color = DangerRedColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UserSettingsScreenPreview() {
    MaterialTheme {
        UserSettingsScreen(
            user = User(username = "username", email = "user@example.com", role = "Reviewer"),
            userReviews = listOf(
                Review(
                    id = "rev_1",
                    title = "Review title",
                    author = "Author",
                    content = "Sample content",
                    createdAt = "2025-01-01",
                    updatedAt = "2025-01-01",
                    commentCount = 14,
                    likeCount = 45,
                    assets = emptyList()
                )
            ),
            onBackClick = {},
            onUpdateUsername = {},
            onUpdateEmail = {},
            onDeleteAccount = {},
            onLogout = {}
        )
    }
}
