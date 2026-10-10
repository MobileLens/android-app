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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.auth.model.User
import com.mobilelens.mobilelens.auth.ui.components.AuthErrorText
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
    onDeleteReview: (String) -> Unit = {},
    profileError: String? = null,
    onClearProfileError: () -> Unit = {}
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
                contentDescription = stringResource(R.string.common_back),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Text(
            text = stringResource(R.string.settings_title, user.username),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Section 1: Personal info
        Text(
            text = stringResource(R.string.settings_personal_info),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        PersonalInfoRow(label = stringResource(R.string.auth_username), value = user.username)
        PersonalInfoRow(label = stringResource(R.string.auth_email), value = user.email)
        PersonalInfoRow(label = stringResource(R.string.settings_role), value = roleLabel(user.role))

        Spacer(modifier = Modifier.height(28.dp))

        // Section 2: Change
        Text(
            text = stringResource(R.string.settings_change),
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
                text = stringResource(R.string.auth_username),
                onClick = {
                    newUsernameInput = user.username
                    showChangeUsernameDialog = true
                },
                modifier = Modifier.weight(1f)
            )

            AuthPillButton(
                text = stringResource(R.string.auth_email),
                onClick = {
                    onClearProfileError()
                    newEmailInput = user.email
                    showChangeEmailDialog = true
                },
                modifier = Modifier.weight(1f)
            )

            AuthPillButton(
                text = stringResource(R.string.auth_password),
                onClick = {
                    newPasswordInput = ""
                    showChangePasswordDialog = true
                },
                modifier = Modifier.weight(1f)
            )
        }

        AuthErrorText(message = profileError)

        Spacer(modifier = Modifier.height(28.dp))

        // Section 3: Danger zone
        Text(
            text = stringResource(R.string.settings_danger_zone),
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
                Text(stringResource(R.string.settings_delete_account), color = Color.White)
            }

            Spacer(modifier = Modifier.width(12.dp))

            TextButton(
                onClick = onLogout,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(stringResource(R.string.settings_log_out), color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Section 4: Review history
        Text(
            text = stringResource(R.string.settings_review_history),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (userReviews.isEmpty()) {
            Text(
                text = stringResource(R.string.settings_no_review_history),
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
            title = { Text(stringResource(R.string.settings_change_username_title)) },
            text = {
                AuthTextField(
                    value = newUsernameInput,
                    onValueChange = { newUsernameInput = it },
                    label = stringResource(R.string.settings_new_username)
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
                    Text(stringResource(R.string.common_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangeUsernameDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (showChangeEmailDialog) {
        AlertDialog(
            onDismissRequest = { showChangeEmailDialog = false },
            title = { Text(stringResource(R.string.settings_change_email_title)) },
            text = {
                AuthTextField(
                    value = newEmailInput,
                    onValueChange = { newEmailInput = it },
                    label = stringResource(R.string.settings_new_email)
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
                    Text(stringResource(R.string.common_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangeEmailDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text(stringResource(R.string.settings_change_password_title)) },
            text = {
                AuthTextField(
                    value = newPasswordInput,
                    onValueChange = { newPasswordInput = it },
                    label = stringResource(R.string.settings_new_password),
                    isPassword = true
                )
            },
            confirmButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text(stringResource(R.string.common_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text(stringResource(R.string.settings_delete_account_title)) },
            text = { Text(stringResource(R.string.settings_delete_account_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        onDeleteAccount()
                    }
                ) {
                    Text(stringResource(R.string.common_delete), color = DangerRedColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

// Roles come from the backend as identifiers ("user", "reviewer", ...); unknown ones are shown as-is
@Composable
private fun roleLabel(role: String): String = when (role.lowercase()) {
    "user" -> stringResource(R.string.role_user)
    "reviewer" -> stringResource(R.string.role_reviewer)
    "moderator" -> stringResource(R.string.role_moderator)
    "admin" -> stringResource(R.string.role_admin)
    else -> role
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
