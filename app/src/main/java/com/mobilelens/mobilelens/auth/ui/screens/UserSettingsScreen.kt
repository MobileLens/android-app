package com.mobilelens.mobilelens.auth.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.auth.model.User
import com.mobilelens.mobilelens.auth.ui.components.ChangeEmailDialog
import com.mobilelens.mobilelens.auth.ui.components.ChangePasswordDialog
import com.mobilelens.mobilelens.auth.ui.components.ChangeUsernameDialog
import com.mobilelens.mobilelens.auth.ui.components.DeleteAccountDialog
import com.mobilelens.mobilelens.auth.ui.components.DeleteReviewDialog
import com.mobilelens.mobilelens.auth.ui.components.ProfileHeader
import com.mobilelens.mobilelens.auth.ui.components.UserCameraListItem
import com.mobilelens.mobilelens.auth.ui.components.UserReviewListItem
import com.mobilelens.mobilelens.auth.viewmodel.AccountEditState
import com.mobilelens.mobilelens.phones.model.Facing
import com.mobilelens.mobilelens.phones.model.LensType
import com.mobilelens.mobilelens.phones.model.SubmittedCamera
import com.mobilelens.mobilelens.phones.viewmodel.MyCamerasUiState
import com.mobilelens.mobilelens.reviews.model.Review
import com.mobilelens.mobilelens.reviews.viewmodel.MyReviewsUiState
import com.mobilelens.mobilelens.settings.ui.components.SettingsSectionHeader

/** The account screen's dialogs that save something; only one is open at a time. */
private enum class AccountDialog { Username, Email, Password, DeleteAccount }

/**
 * The signed-in user's account: profile, password, their reviews, logging out and deleting the
 * account. Has its own top bar, so MainApp hides the search bar here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSettingsScreen(
    user: User,
    reviewsState: MyReviewsUiState,
    camerasState: MyCamerasUiState,
    // Progress of whatever the open dialog is saving
    editState: AccountEditState,
    onBackClick: () -> Unit,
    onUpdateUsername: (String) -> Unit,
    onUpdateEmail: (String) -> Unit,
    onChangePassword: (currentPassword: String, newPassword: String) -> Unit,
    onDeleteAccount: () -> Unit,
    onClearEditState: () -> Unit,
    onLogout: () -> Unit,
    onReviewClick: (String) -> Unit,
    onDeleteReview: (String) -> Unit,
    onRetryReviews: () -> Unit,
    onCameraClick: (phoneId: String) -> Unit,
    onRetryCameras: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var openDialog by rememberSaveable { mutableStateOf<AccountDialog?>(null) }
    var reviewToDeleteId by rememberSaveable { mutableStateOf<String?>(null) }
    // The screen's background shows through, as on the other screens
    val itemColors = ListItemDefaults.colors(containerColor = Color.Transparent)

    // A dialog closes once its change is saved, and the route confirms it in the snackbar
    LaunchedEffect(editState) {
        if (editState is AccountEditState.Saved) openDialog = null
    }

    // Opening or closing a dialog drops the result of the previous one
    fun showDialog(dialog: AccountDialog?) {
        onClearEditState()
        openDialog = dialog
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.account)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
                // Insets are handled by the parent Scaffold
                windowInsets = WindowInsets(0, 0, 0, 0),
                scrollBehavior = scrollBehavior,
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item(key = "header") {
                    ProfileHeader(user = user)
                }

                item(key = "profile") {
                    SettingsSectionHeader(text = stringResource(R.string.settings_section_profile))
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.auth_username)) },
                        supportingContent = { Text(user.username) },
                        leadingContent = { Icon(Icons.Outlined.Person, contentDescription = null) },
                        modifier = Modifier.clickable(role = Role.Button) { showDialog(AccountDialog.Username) },
                        colors = itemColors,
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.auth_email)) },
                        supportingContent = { Text(user.email) },
                        leadingContent = { Icon(Icons.Outlined.Email, contentDescription = null) },
                        modifier = Modifier.clickable(role = Role.Button) { showDialog(AccountDialog.Email) },
                        colors = itemColors,
                    )
                }

                item(key = "security") {
                    SettingsSectionHeader(text = stringResource(R.string.settings_section_security))
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.auth_password)) },
                        supportingContent = { Text(stringResource(R.string.settings_password_supporting)) },
                        leadingContent = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                        modifier = Modifier.clickable(role = Role.Button) { showDialog(AccountDialog.Password) },
                        colors = itemColors,
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_log_out)) },
                        supportingContent = { Text(stringResource(R.string.settings_log_out_supporting)) },
                        leadingContent = { Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null) },
                        modifier = Modifier.clickable(role = Role.Button, onClick = onLogout),
                        colors = itemColors,
                    )
                }

                item(key = "reviews_header") {
                    SettingsSectionHeader(text = stringResource(R.string.settings_section_reviews))
                }
                reviewsSection(
                    state = reviewsState,
                    onReviewClick = onReviewClick,
                    onDeleteClick = { review -> reviewToDeleteId = review.id },
                    onRetry = onRetryReviews,
                )

                item(key = "cameras_header") {
                    SettingsSectionHeader(text = stringResource(R.string.settings_section_cameras))
                }
                camerasSection(
                    state = camerasState,
                    onCameraClick = onCameraClick,
                    onRetry = onRetryCameras,
                )

                item(key = "danger_zone") {
                    SettingsSectionHeader(text = stringResource(R.string.settings_danger_zone))
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_delete_account)) },
                        supportingContent = { Text(stringResource(R.string.settings_delete_account_supporting)) },
                        leadingContent = { Icon(Icons.Outlined.DeleteForever, contentDescription = null) },
                        modifier = Modifier.clickable(role = Role.Button) { showDialog(AccountDialog.DeleteAccount) },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent,
                            headlineColor = MaterialTheme.colorScheme.error,
                            leadingIconColor = MaterialTheme.colorScheme.error,
                        ),
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    val saving = editState == AccountEditState.Saving
    val editError = (editState as? AccountEditState.Failed)?.let { stringResource(it.messageRes) }
    when (openDialog) {
        AccountDialog.Username -> ChangeUsernameDialog(
            currentUsername = user.username,
            saving = saving,
            errorMessage = editError,
            onSave = onUpdateUsername,
            onClearError = onClearEditState,
            onDismiss = { showDialog(null) },
        )
        AccountDialog.Email -> ChangeEmailDialog(
            currentEmail = user.email,
            saving = saving,
            errorMessage = editError,
            onSave = onUpdateEmail,
            onClearError = onClearEditState,
            onDismiss = { showDialog(null) },
        )
        AccountDialog.Password -> ChangePasswordDialog(
            saving = saving,
            errorMessage = editError,
            onSave = onChangePassword,
            onClearError = onClearEditState,
            onDismiss = { showDialog(null) },
        )
        AccountDialog.DeleteAccount -> DeleteAccountDialog(
            deleting = saving,
            errorMessage = editError,
            onConfirm = onDeleteAccount,
            onDismiss = { showDialog(null) },
        )
        null -> {}
    }

    // Looked up again, since the list may have changed while the dialog was open
    val reviewToDelete = (reviewsState as? MyReviewsUiState.Success)?.reviews
        ?.firstOrNull { it.id == reviewToDeleteId }
    if (reviewToDelete != null) {
        DeleteReviewDialog(
            reviewTitle = reviewToDelete.title,
            onConfirm = {
                reviewToDeleteId = null
                onDeleteReview(reviewToDelete.id)
            },
            onDismiss = { reviewToDeleteId = null },
        )
    }
}

private fun LazyListScope.reviewsSection(
    state: MyReviewsUiState,
    onReviewClick: (String) -> Unit,
    onDeleteClick: (Review) -> Unit,
    onRetry: () -> Unit,
) {
    when (state) {
        MyReviewsUiState.Loading -> item(key = "reviews_loading") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        is MyReviewsUiState.Error -> item(key = "reviews_error") {
            ListItem(
                headlineContent = { Text(stringResource(state.messageRes)) },
                trailingContent = {
                    TextButton(onClick = onRetry) {
                        Text(stringResource(R.string.action_retry))
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }
        is MyReviewsUiState.Success -> if (state.reviews.isEmpty()) {
            item(key = "reviews_empty") {
                Text(
                    text = stringResource(R.string.settings_no_reviews),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        } else {
            items(state.reviews, key = { it.id }) { review ->
                UserReviewListItem(
                    review = review,
                    deleting = review.id in state.deletingIds,
                    onClick = { onReviewClick(review.id) },
                    onDelete = { onDeleteClick(review) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

private fun LazyListScope.camerasSection(
    state: MyCamerasUiState,
    onCameraClick: (phoneId: String) -> Unit,
    onRetry: () -> Unit,
) {
    when (state) {
        MyCamerasUiState.Loading -> item(key = "cameras_loading") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        is MyCamerasUiState.Error -> item(key = "cameras_error") {
            ListItem(
                headlineContent = { Text(stringResource(state.messageRes)) },
                trailingContent = {
                    TextButton(onClick = onRetry) {
                        Text(stringResource(R.string.action_retry))
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }
        is MyCamerasUiState.Success -> if (state.cameras.isEmpty()) {
            item(key = "cameras_empty") {
                Text(
                    text = stringResource(R.string.settings_no_cameras),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        } else {
            items(state.cameras, key = { it.id }) { camera ->
                UserCameraListItem(
                    camera = camera,
                    onClick = { onCameraClick(camera.phoneId) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

private val previewCameras = listOf(
    SubmittedCamera(
        id = "cam_1",
        phoneId = "phone_1",
        phoneName = "Pixel 9 Pro",
        type = LensType.WIDE,
        facing = Facing.BACK,
        focalLengthMm = 6.9,
        resolutionMp = 50.0,
        status = "approved",
        submittedAt = "2026-10-03T10:00:00.000Z",
    ),
    SubmittedCamera(
        id = "cam_2",
        phoneId = "phone_1",
        phoneName = "Pixel 9 Pro",
        type = LensType.TELEPHOTO,
        facing = Facing.BACK,
        focalLengthMm = 11.0,
        resolutionMp = 48.0,
        status = "pending",
        submittedAt = "2026-10-09T10:00:00.000Z",
    ),
)

private val previewReviews = listOf(
    Review(
        id = "rev_1",
        title = "Pixel 9 Pro: the telephoto finally earns its place",
        author = "maciek",
        content = "",
        createdAt = "2026-09-19T10:00:00.000Z",
        updatedAt = "2026-10-03T10:00:00.000Z",
        commentCount = 3,
        likeCount = 12,
        assets = emptyList(),
        status = "published",
    ),
    Review(
        id = "rev_2",
        title = "Galaxy S24 Ultra night mode",
        author = "maciek",
        content = "",
        createdAt = "2026-10-09T10:00:00.000Z",
        updatedAt = "2026-10-09T10:00:00.000Z",
        commentCount = 0,
        likeCount = 0,
        assets = emptyList(),
        status = "pending",
    ),
)

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun UserSettingsScreenPreview() {
    MaterialTheme {
        UserSettingsScreen(
            user = User(username = "maciek", email = "maciek@example.com", role = "reviewer"),
            reviewsState = MyReviewsUiState.Success(previewReviews),
            camerasState = MyCamerasUiState.Success(previewCameras),
            editState = AccountEditState.Idle,
            onBackClick = {},
            onUpdateUsername = {},
            onUpdateEmail = {},
            onChangePassword = { _, _ -> },
            onDeleteAccount = {},
            onClearEditState = {},
            onLogout = {},
            onReviewClick = {},
            onDeleteReview = {},
            onRetryReviews = {},
            onCameraClick = {},
            onRetryCameras = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UserSettingsScreenNoReviewsPreview() {
    MaterialTheme {
        UserSettingsScreen(
            user = User(username = "maciek", email = "maciek@example.com", role = "user"),
            reviewsState = MyReviewsUiState.Success(emptyList()),
            camerasState = MyCamerasUiState.Success(emptyList()),
            editState = AccountEditState.Idle,
            onBackClick = {},
            onUpdateUsername = {},
            onUpdateEmail = {},
            onChangePassword = { _, _ -> },
            onDeleteAccount = {},
            onClearEditState = {},
            onLogout = {},
            onReviewClick = {},
            onDeleteReview = {},
            onRetryReviews = {},
            onCameraClick = {},
            onRetryCameras = {},
        )
    }
}
