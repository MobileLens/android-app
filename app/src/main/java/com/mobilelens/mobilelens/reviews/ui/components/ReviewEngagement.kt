package com.mobilelens.mobilelens.reviews.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.ui.localizedDate
import com.mobilelens.mobilelens.reviews.model.ReviewComment
import com.mobilelens.mobilelens.reviews.viewmodel.COMMENT_MAX_LENGTH
import com.mobilelens.mobilelens.reviews.viewmodel.CommentsUiState

/** The like toggle with the review's like count beside it. */
@Composable
fun ReviewLikeButton(
    liked: Boolean,
    likeCount: Int,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        IconToggleButton(checked = liked, onCheckedChange = { onToggle() }) {
            Icon(
                imageVector = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = stringResource(
                    if (liked) R.string.review_unlike else R.string.review_like
                ),
                tint = if (liked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = likeCount.toString(),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

/**
 * The discussion under a published review: the comments, oldest first, and a box to write a new
 * one, or a prompt to log in. [canDelete] says which comments the viewer may remove.
 */
@Composable
fun ReviewCommentsSection(
    state: CommentsUiState,
    draft: String,
    isPosting: Boolean,
    isSignedIn: Boolean,
    canDelete: (ReviewComment) -> Boolean,
    onDraftChange: (String) -> Unit,
    onPost: () -> Unit,
    onDeleteClick: (ReviewComment) -> Unit,
    onRetry: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        val count = (state as? CommentsUiState.Success)?.comments?.size
        Text(
            text = if (count != null) {
                stringResource(R.string.review_comments_title_count, count)
            } else {
                stringResource(R.string.review_comments)
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )

        if (isSignedIn) {
            CommentComposer(
                draft = draft,
                isPosting = isPosting,
                // The list has to be loaded first, so a new comment lands in the right place
                enabled = state is CommentsUiState.Success,
                onDraftChange = onDraftChange,
                onPost = onPost,
                modifier = Modifier.padding(top = 12.dp),
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Text(
                    text = stringResource(R.string.review_comment_login_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onLoginClick) {
                    Text(stringResource(R.string.review_comment_login_action))
                }
            }
        }

        when (state) {
            CommentsUiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            is CommentsUiState.Error -> Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(
                    text = stringResource(state.messageRes),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onRetry) {
                    Text(stringResource(R.string.action_retry))
                }
            }
            is CommentsUiState.Success -> if (state.comments.isEmpty()) {
                Text(
                    text = stringResource(R.string.review_no_comments),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp),
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 16.dp),
                ) {
                    state.comments.forEach { comment ->
                        CommentItem(
                            comment = comment,
                            deleting = comment.id in state.deletingIds,
                            canDelete = canDelete(comment),
                            onDelete = { onDeleteClick(comment) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentComposer(
    draft: String,
    isPosting: Boolean,
    enabled: Boolean,
    onDraftChange: (String) -> Unit,
    onPost: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = draft,
        onValueChange = onDraftChange,
        enabled = !isPosting,
        placeholder = { Text(stringResource(R.string.review_comment_hint)) },
        supportingText = if (draft.length > COMMENT_MAX_LENGTH * 4 / 5) {
            { Text("${draft.length}/$COMMENT_MAX_LENGTH") }
        } else {
            null
        },
        trailingIcon = {
            if (isPosting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                IconButton(onClick = onPost, enabled = enabled && draft.isNotBlank()) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.review_comment_send),
                    )
                }
            }
        },
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun CommentItem(
    comment: ReviewComment,
    deleting: Boolean,
    canDelete: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.author,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = " · ${localizedDate(comment.createdAt)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (deleting) {
            // Same footprint as the button, so the row doesn't shift
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        } else if (canDelete) {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.common_delete),
                )
            }
        }
    }
}

private val previewComments = listOf(
    ReviewComment(
        id = "c1",
        authorId = "u1",
        author = "maciek",
        content = "Great write-up, the night shots especially.",
        createdAt = "2026-10-03T10:00:00.000Z",
    ),
    ReviewComment(
        id = "c2",
        authorId = "u2",
        author = "ola",
        content = "Does the telephoto hold up indoors?",
        createdAt = "2026-10-04T18:30:00.000Z",
    ),
)

@Preview(showBackground = true)
@Composable
private fun ReviewCommentsSectionPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ReviewLikeButton(liked = true, likeCount = 12, onToggle = {})
            ReviewCommentsSection(
                state = CommentsUiState.Success(previewComments),
                draft = "",
                isPosting = false,
                isSignedIn = true,
                canDelete = { it.authorId == "u1" },
                onDraftChange = {},
                onPost = {},
                onDeleteClick = {},
                onRetry = {},
                onLoginClick = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReviewCommentsSectionSignedOutPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ReviewCommentsSection(
                state = CommentsUiState.Success(emptyList()),
                draft = "",
                isPosting = false,
                isSignedIn = false,
                canDelete = { false },
                onDraftChange = {},
                onPost = {},
                onDeleteClick = {},
                onRetry = {},
                onLoginClick = {},
            )
        }
    }
}
