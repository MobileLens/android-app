package com.mobilelens.mobilelens.reviews.navigation

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.auth.viewmodel.AuthViewModel
import com.mobilelens.mobilelens.core.navigation.Screen
import com.mobilelens.mobilelens.core.ui.ErrorContent
import com.mobilelens.mobilelens.core.ui.LoadingContent
import com.mobilelens.mobilelens.core.ui.UiStateCrossfade
import com.mobilelens.mobilelens.reviews.ui.screens.ReviewScreen
import com.mobilelens.mobilelens.reviews.ui.screens.ReviewThreadScreen
import com.mobilelens.mobilelens.reviews.ui.screens.WriteReviewScreen
import com.mobilelens.mobilelens.reviews.viewmodel.CommentsViewModel
import com.mobilelens.mobilelens.reviews.viewmodel.PublishReviewUiState
import com.mobilelens.mobilelens.reviews.viewmodel.ReviewDetailsUiState
import com.mobilelens.mobilelens.reviews.viewmodel.ReviewThreadUiState
import com.mobilelens.mobilelens.reviews.viewmodel.ReviewViewModel
import com.mobilelens.mobilelens.reviews.viewmodel.WriteReviewViewModel

/** ReviewThread, ReviewDetails and WriteReview. */
fun NavGraphBuilder.reviewsRoutes(
    navController: NavController,
    authViewModel: AuthViewModel,
) {
    composable<Screen.ReviewThread> { backStackEntry ->
        val route = backStackEntry.toRoute<Screen.ReviewThread>()
        val reviewViewModel: ReviewViewModel = viewModel()
        val threadState by reviewViewModel.thread.collectAsState()
        val likeMessageRes by reviewViewModel.message.collectAsState()
        val currentUser by authViewModel.currentUser.collectAsState()
        val context = LocalContext.current
        val resources = LocalResources.current
        val shareChooserTitle = stringResource(R.string.review_share)

        LaunchedEffect(route.phoneId) {
            reviewViewModel.loadReviewsForPhone(route.phoneId)
        }

        // A toast, since the thread has no snackbar host of its own
        val likeMessage = likeMessageRes?.let { stringResource(it) }
        LaunchedEffect(likeMessage) {
            if (likeMessage != null) {
                Toast.makeText(context, likeMessage, Toast.LENGTH_SHORT).show()
                reviewViewModel.messageShown()
            }
        }

        UiStateCrossfade(state = threadState) { state ->
            when (state) {
                is ReviewThreadUiState.Loading -> LoadingContent()
                is ReviewThreadUiState.Error -> ErrorContent(messageRes = state.messageRes)
                is ReviewThreadUiState.Success -> {
                    ReviewThreadScreen(
                        thread = state.thread,
                        onReviewClick = { review ->
                            navController.navigate(Screen.ReviewDetails(reviewId = review.id))
                        },
                        // The comments live on the review's own screen
                        onCommentClick = { review ->
                            navController.navigate(
                                Screen.ReviewDetails(reviewId = review.id, scrollToComments = true)
                            )
                        },
                        // A signed-out viewer can't like, so the heart sends them to log in
                        onLikeClick = { review ->
                            if (currentUser != null) {
                                reviewViewModel.toggleThreadLike(review.id)
                            } else {
                                navController.navigate(Screen.Login)
                            }
                        },
                        onShareClick = { review ->
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, review.title)
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    resources.getString(R.string.review_share_text, review.title, review.author),
                                )
                            }
                            context.startActivity(Intent.createChooser(send, shareChooserTitle))
                        },
                    )
                }
            }
        }
    }
    composable<Screen.ReviewDetails> { backStackEntry ->
        val route = backStackEntry.toRoute<Screen.ReviewDetails>()
        val reviewViewModel: ReviewViewModel = viewModel()
        val commentsViewModel: CommentsViewModel = viewModel()
        val selectedReviewState by reviewViewModel.selectedReview.collectAsState()
        val commentsState by commentsViewModel.comments.collectAsState()
        val isPosting by commentsViewModel.isPosting.collectAsState()
        val likeMessageRes by reviewViewModel.message.collectAsState()
        val commentMessageRes by commentsViewModel.message.collectAsState()
        val currentUser by authViewModel.currentUser.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(route.reviewId) {
            reviewViewModel.loadReview(route.reviewId)
            commentsViewModel.loadComments(route.reviewId)
        }

        val likeMessage = likeMessageRes?.let { stringResource(it) }
        LaunchedEffect(likeMessage) {
            if (likeMessage != null) {
                snackbarHostState.showSnackbar(likeMessage)
                reviewViewModel.messageShown()
            }
        }
        val commentMessage = commentMessageRes?.let { stringResource(it) }
        LaunchedEffect(commentMessage) {
            if (commentMessage != null) {
                snackbarHostState.showSnackbar(commentMessage)
                commentsViewModel.messageShown()
            }
        }

        UiStateCrossfade(state = selectedReviewState) { state ->
            when (state) {
                is ReviewDetailsUiState.Loading -> LoadingContent()
                is ReviewDetailsUiState.Error -> ErrorContent(messageRes = state.messageRes)
                is ReviewDetailsUiState.Success -> {
                    val user = currentUser
                    ReviewScreen(
                        review = state.review,
                        scrollToComments = route.scrollToComments,
                        commentsState = commentsState,
                        draft = commentsViewModel.draft,
                        isPosting = isPosting,
                        isSignedIn = user != null,
                        // The author removes their own comments, an admin anyone's
                        canDeleteComment = { comment ->
                            user != null && (comment.authorId == user.id || user.role == "admin")
                        },
                        // A signed-out viewer can't like, so the heart sends them to log in
                        onToggleLike = {
                            if (user != null) {
                                reviewViewModel.toggleLike()
                            } else {
                                navController.navigate(Screen.Login)
                            }
                        },
                        onDraftChange = commentsViewModel::updateDraft,
                        onPostComment = { commentsViewModel.postComment(route.reviewId) },
                        onDeleteComment = { commentId ->
                            commentsViewModel.deleteComment(route.reviewId, commentId)
                        },
                        onRetryComments = { commentsViewModel.loadComments(route.reviewId) },
                        onLoginClick = { navController.navigate(Screen.Login) },
                        snackbarHostState = snackbarHostState,
                    )
                }
            }
        }
    }
    composable<Screen.WriteReview> { backStackEntry ->
        val route = backStackEntry.toRoute<Screen.WriteReview>()
        val writeReviewViewModel: WriteReviewViewModel = viewModel()
        val target by writeReviewViewModel.target.collectAsState()
        val isUploadingImage by writeReviewViewModel.isUploadingImage.collectAsState()
        val publishState by writeReviewViewModel.publishState.collectAsState()
        val errorMessageRes by writeReviewViewModel.errorMessage.collectAsState()

        val context = LocalContext.current
        val snackbarHostState = remember { SnackbarHostState() }
        val pickImage = rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                writeReviewViewModel.insertImage(context.contentResolver, uri)
            }
        }

        LaunchedEffect(route.phoneId) {
            writeReviewViewModel.loadTarget(route.phoneId)
        }

        val errorMessage = errorMessageRes?.let { stringResource(it) }
        LaunchedEffect(errorMessage) {
            if (errorMessage != null) {
                // Cleared only afterwards: clearing changes this effect's key, which would cancel it
                snackbarHostState.showSnackbar(errorMessage)
                writeReviewViewModel.errorMessageShown()
            }
        }

        val moderationMessage = stringResource(R.string.review_editor_published)
        val liveMessage = stringResource(R.string.review_editor_published_live)
        LaunchedEffect(publishState) {
            val state = publishState
            if (state is PublishReviewUiState.Published) {
                // A toast, since the editor is gone by the time a snackbar could show
                val message = if (state.inModeration) moderationMessage else liveMessage
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
        }

        WriteReviewScreen(
            title = writeReviewViewModel.title,
            content = writeReviewViewModel.content,
            target = target,
            isUploadingImage = isUploadingImage,
            isPublishing = publishState != PublishReviewUiState.Idle,
            snackbarHostState = snackbarHostState,
            // Ignores repeated taps, which would otherwise pop past the previous screen
            onClose = dropUnlessResumed { navController.popBackStack() },
            onPublish = writeReviewViewModel::publish,
            onInsertImage = {
                if (!isUploadingImage) {
                    pickImage.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            },
        )
    }
}
