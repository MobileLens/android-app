package com.mobilelens.mobilelens.auth.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.auth.model.User
import com.mobilelens.mobilelens.core.ui.UserAvatar

/** The signed-in user's avatar, name, e-mail and role, at the top of the account screen. */
@Composable
fun ProfileHeader(
    user: User,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        UserAvatar(
            initial = user.username.take(1).uppercase().ifEmpty { null },
            size = 88.dp,
            textStyle = MaterialTheme.typography.displaySmall,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = user.username,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(12.dp))
        RoleBadge(role = user.role)
    }
}

@Composable
private fun RoleBadge(role: String) {
    val label = roleLabel(role)
    val description = stringResource(R.string.settings_role_description, label)
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        // Read as "Role: Reviewer" rather than a bare "Reviewer"
        modifier = Modifier.clearAndSetSemantics { contentDescription = description },
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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
private fun ProfileHeaderPreview() {
    MaterialTheme {
        ProfileHeader(user = User(username = "maciek", email = "maciek@example.com", role = "reviewer"))
    }
}
