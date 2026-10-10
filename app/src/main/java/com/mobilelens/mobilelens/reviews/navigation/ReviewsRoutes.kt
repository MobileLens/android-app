package com.mobilelens.mobilelens.reviews.navigation

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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.navigation.Screen
import com.mobilelens.mobilelens.core.ui.ErrorContent
import com.mobilelens.mobilelens.core.ui.LoadingContent
import com.mobilelens.mobilelens.core.ui.UiStateCrossfade
import com.mobilelens.mobilelens.reviews.ui.screens.ReviewScreen
import com.mobilelens.mobilelens.reviews.ui.screens.ReviewThreadScreen
import com.mobilelens.mobilelens.reviews.ui.screens.WriteReviewScreen
import com.mobilelens.mobilelens.reviews.viewmodel.PublishReviewUiState
import com.mobilelens.mobilelens.reviews.viewmodel.ReviewDetailsUiState
import com.mobilelens.mobilelens.reviews.viewmodel.ReviewThreadUiState
import com.mobilelens.mobilelens.reviews.viewmodel.ReviewViewModel
import com.mobilelens.mobilelens.reviews.viewmodel.WriteReviewViewModel

/** ReviewThread, ReviewDetails and WriteReview. */
fun NavGraphBuilder.reviewsRoutes(navController: NavController) {
    composable<Screen.ReviewThread> { backStackEntry ->
        val route = backStackEntry.toRoute<Screen.ReviewThread>()
        val reviewViewModel: ReviewViewModel = viewModel()
        val threadState by reviewViewModel.thread.collectAsState()

        LaunchedEffect(route.phoneId) {
            reviewViewModel.loadReviewsForPhone(route.phoneId)
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
                        }
                    )
                }
            }
        }
    }
    composable<Screen.ReviewDetails> { backStackEntry ->
        val route = backStackEntry.toRoute<Screen.ReviewDetails>()
        val reviewViewModel: ReviewViewModel = viewModel()
        val selectedReviewState by reviewViewModel.selectedReview.collectAsState()

        LaunchedEffect(route.reviewId) {
            reviewViewModel.loadReview(route.reviewId)
        }

        UiStateCrossfade(state = selectedReviewState) { state ->
            when (state) {
                is ReviewDetailsUiState.Loading -> LoadingContent()
                is ReviewDetailsUiState.Error -> ErrorContent(messageRes = state.messageRes)
                is ReviewDetailsUiState.Success -> ReviewScreen(review = state.review)
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
