package com.mobilelens.mobilelens.auth.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.ui.localizedDate
import com.mobilelens.mobilelens.phones.model.Facing
import com.mobilelens.mobilelens.phones.model.LensType
import com.mobilelens.mobilelens.phones.model.SubmittedCamera
import com.mobilelens.mobilelens.phones.ui.labelRes

/**
 * One of the cameras the user submitted: the lens, the phone it belongs to, and where it stands in
 * moderation. Tapping it opens that phone.
 */
@Composable
fun UserCameraListItem(
    camera: SubmittedCamera,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val approved = camera.status == "approved"
    val locale = LocalConfiguration.current.locales[0]
    val lensLabel = stringResource(
        R.string.lens_tab_label,
        stringResource(camera.facing.labelRes()),
        stringResource(camera.type.labelRes()).lowercase(locale),
    )
    val status = statusLabel(camera.status)
    val statusLine = camera.submittedAt?.let {
        stringResource(R.string.settings_camera_status_date, status, localizedDate(it))
    } ?: status

    ListItem(
        headlineContent = {
            Text(
                text = stringResource(
                    R.string.settings_camera_headline,
                    lensLabel,
                    stringResource(R.string.value_focal_length, camera.focalLengthMm),
                    stringResource(R.string.value_megapixels, camera.resolutionMp),
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Column {
                Text(
                    text = camera.phoneName ?: stringResource(R.string.settings_camera_unknown_phone),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(text = statusLine, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        leadingContent = {
            Icon(
                imageVector = statusIcon(camera.status),
                contentDescription = null,
                tint = when {
                    approved -> MaterialTheme.colorScheme.primary
                    camera.status == "rejected" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        },
        modifier = modifier.clickable(role = Role.Button, onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

// Statuses as in the backend schema: pending | approved | rejected
@Composable
private fun statusLabel(status: String): String = when (status) {
    "approved" -> stringResource(R.string.settings_camera_status_approved)
    "rejected" -> stringResource(R.string.settings_camera_status_rejected)
    "pending" -> stringResource(R.string.settings_camera_status_pending)
    else -> status
}

private fun statusIcon(status: String): ImageVector = when (status) {
    "approved" -> Icons.Outlined.CheckCircle
    "rejected" -> Icons.Outlined.Block
    else -> Icons.Outlined.Schedule
}

private fun previewCamera(status: String) = SubmittedCamera(
    id = "cam_$status",
    phoneId = "phone_1",
    phoneName = "Pixel 9 Pro",
    type = LensType.WIDE,
    facing = Facing.BACK,
    focalLengthMm = 6.9,
    resolutionMp = 50.0,
    status = status,
    submittedAt = "2026-10-03T10:00:00.000Z",
)

@Preview(showBackground = true)
@Composable
private fun UserCameraListItemPreview() {
    MaterialTheme {
        Column {
            UserCameraListItem(camera = previewCamera("approved"), onClick = {})
            UserCameraListItem(camera = previewCamera("pending"), onClick = {})
            UserCameraListItem(camera = previewCamera("rejected").copy(phoneName = null), onClick = {})
        }
    }
}
