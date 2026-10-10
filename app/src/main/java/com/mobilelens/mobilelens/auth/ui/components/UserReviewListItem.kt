package com.mobilelens.mobilelens.auth.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.ui.localizedDate
import com.mobilelens.mobilelens.reviews.model.Review

/**
 * One of the user's own reviews: its status and last update, plus likes and comments once it's
 * public. Tapping it opens the review; [deleting] swaps the delete button for a spinner.
 */
@Composable
fun UserReviewListItem(
    review: Review,
    deleting: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val published = review.status == "published"
    ListItem(
        headlineContent = {
            Text(text = review.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(
                        R.string.settings_review_status_date,
                        statusLabel(review.status),
                        localizedDate(review.updatedAt),
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                // Only a published review can be liked or commented on
                if (published) {
                    Spacer(modifier = Modifier.width(12.dp))
                    ReviewCount(
                        icon = Icons.Outlined.FavoriteBorder,
                        description = stringResource(R.string.review_likes),
                        count = review.likeCount,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ReviewCount(
                        icon = Icons.AutoMirrored.Outlined.Comment,
                        description = stringResource(R.string.review_comments),
                        count = review.commentCount,
                    )
                }
            }
        },
        leadingContent = {
            Icon(
                imageVector = statusIcon(review.status),
                contentDescription = null,
                tint = if (published) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            if (deleting) {
                // Same footprint as the button, so the row doesn't shift
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            } else {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.common_delete),
                    )
                }
            }
        },
        modifier = modifier.clickable(enabled = !deleting, role = Role.Button, onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
private fun ReviewCount(icon: ImageVector, description: String, count: Int) {
    Icon(
        imageVector = icon,
        contentDescription = description,
        modifier = Modifier
            .padding(end = 4.dp)
            .size(16.dp),
    )
    Text(text = count.toString())
}

// Statuses as in the backend schema: draft | pending | published | hidden
@Composable
private fun statusLabel(status: String): String = when (status) {
    "published" -> stringResource(R.string.settings_review_status_published)
    "pending" -> stringResource(R.string.settings_review_status_pending)
    "hidden" -> stringResource(R.string.settings_review_status_hidden)
    "draft" -> stringResource(R.string.settings_review_status_draft)
    else -> status
}

private fun statusIcon(status: String): ImageVector = when (status) {
    "published" -> Icons.Outlined.Public
    "hidden" -> Icons.Outlined.VisibilityOff
    "draft" -> Icons.Outlined.EditNote
    else -> Icons.Outlined.Schedule
}

private fun previewReview(status: String) = Review(
    id = "rev_$status",
    title = "Pixel 9 Pro: the telephoto finally earns its place",
    author = "maciek",
    content = "",
    createdAt = "2026-09-19T10:00:00.000Z",
    updatedAt = "2026-10-03T10:00:00.000Z",
    commentCount = 3,
    likeCount = 12,
    assets = emptyList(),
    status = status,
)

@Preview(showBackground = true)
@Composable
private fun UserReviewListItemPreview() {
    MaterialTheme {
        Column {
            UserReviewListItem(review = previewReview("published"), deleting = false, onClick = {}, onDelete = {})
            UserReviewListItem(review = previewReview("pending"), deleting = false, onClick = {}, onDelete = {})
            UserReviewListItem(review = previewReview("hidden"), deleting = true, onClick = {}, onDelete = {})
        }
    }
}
