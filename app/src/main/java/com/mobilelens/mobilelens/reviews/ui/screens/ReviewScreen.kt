package com.mobilelens.mobilelens.reviews.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.reviews.model.Review
import com.mobilelens.mobilelens.reviews.model.ReviewComment
import com.mobilelens.mobilelens.reviews.ui.components.ReviewCommentsSection
import com.mobilelens.mobilelens.reviews.ui.components.ReviewHeader
import com.mobilelens.mobilelens.reviews.ui.components.ReviewLikeButton
import com.mobilelens.mobilelens.reviews.viewmodel.CommentsUiState

/**
 * A review with its likes and comments. Only a published review can be liked or commented on, so
 * the others (the author's own, still in moderation) show just the text.
 */
@Composable
fun ReviewScreen(
    review: Review,
    commentsState: CommentsUiState,
    draft: String,
    isPosting: Boolean,
    isSignedIn: Boolean,
    canDeleteComment: (ReviewComment) -> Boolean,
    onToggleLike: () -> Unit,
    onDraftChange: (String) -> Unit,
    onPostComment: () -> Unit,
    onDeleteComment: (String) -> Unit,
    onRetryComments: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    var commentToDeleteId by rememberSaveable { mutableStateOf<String?>(null) }
    val published = review.status == "published"

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            ReviewHeader(
                title = review.title,
                author = review.author,
                createdAt = review.createdAt,
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))

            RichText {
                Markdown(review.content)
            }

            if (published) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                ReviewLikeButton(
                    liked = review.likedByMe,
                    likeCount = review.likeCount,
                    onToggle = onToggleLike,
                )
                Spacer(modifier = Modifier.height(8.dp))
                ReviewCommentsSection(
                    state = commentsState,
                    draft = draft,
                    isPosting = isPosting,
                    isSignedIn = isSignedIn,
                    canDelete = canDeleteComment,
                    onDraftChange = onDraftChange,
                    onPost = onPostComment,
                    onDeleteClick = { comment -> commentToDeleteId = comment.id },
                    onRetry = onRetryComments,
                    onLoginClick = onLoginClick,
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    // Looked up again, since the list may have changed while the dialog was open
    val commentToDelete = (commentsState as? CommentsUiState.Success)?.comments
        ?.firstOrNull { it.id == commentToDeleteId }
    if (commentToDelete != null) {
        AlertDialog(
            onDismissRequest = { commentToDeleteId = null },
            title = { Text(stringResource(R.string.review_delete_comment_title)) },
            text = { Text(stringResource(R.string.review_delete_comment_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        commentToDeleteId = null
                        onDeleteComment(commentToDelete.id)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.common_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { commentToDeleteId = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewReviewScreen() {
    MaterialTheme {
        ReviewScreen(
            review = Review(
                id = "0",
                title = "Review title",
                author = "Author",
                content = """
                    # First paragraph

                    Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor
                    incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis
                    nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.
                    Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore
                    eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat
                    non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.

                    # Second paragraph

                    Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium
                    doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore
                    veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim
                    ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia
                    consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt.
                """.trimIndent(),
                createdAt = "01-07-2026",
                updatedAt = "01-07-2026",
                commentCount = 2,
                likeCount = 67,
                likedByMe = true,
                assets = emptyList()
            ),
            commentsState = CommentsUiState.Success(
                listOf(
                    ReviewComment(
                        id = "c1",
                        authorId = "u1",
                        author = "maciek",
                        content = "Great write-up.",
                        createdAt = "2026-10-03T10:00:00.000Z",
                    ),
                )
            ),
            draft = "",
            isPosting = false,
            isSignedIn = true,
            canDeleteComment = { true },
            onToggleLike = {},
            onDraftChange = {},
            onPostComment = {},
            onDeleteComment = {},
            onRetryComments = {},
            onLoginClick = {},
        )
    }
}
