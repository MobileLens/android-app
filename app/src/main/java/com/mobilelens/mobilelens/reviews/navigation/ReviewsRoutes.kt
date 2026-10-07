package com.mobilelens.mobilelens.reviews.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.mobilelens.mobilelens.core.navigation.Screen
import com.mobilelens.mobilelens.core.ui.ErrorContent
import com.mobilelens.mobilelens.core.ui.LoadingContent
import com.mobilelens.mobilelens.reviews.ui.screens.ReviewScreen
import com.mobilelens.mobilelens.reviews.ui.screens.ReviewThreadScreen
import com.mobilelens.mobilelens.reviews.viewmodel.ReviewDetailsUiState
import com.mobilelens.mobilelens.reviews.viewmodel.ReviewThreadUiState
import com.mobilelens.mobilelens.reviews.viewmodel.ReviewViewModel

/** ReviewThread and ReviewDetails. */
fun NavGraphBuilder.reviewsRoutes(navController: NavController) {
    composable<Screen.ReviewThread> { backStackEntry ->
        val route = backStackEntry.toRoute<Screen.ReviewThread>()
        val reviewViewModel: ReviewViewModel = viewModel()
        val threadState by reviewViewModel.thread.collectAsState()

        LaunchedEffect(route.phoneId) {
            reviewViewModel.loadReviewsForPhone(route.phoneId)
        }

        when (val state = threadState) {
            is ReviewThreadUiState.Loading -> LoadingContent()
            is ReviewThreadUiState.Error -> ErrorContent(message = state.message)
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
    composable<Screen.ReviewDetails> { backStackEntry ->
        val route = backStackEntry.toRoute<Screen.ReviewDetails>()
        val reviewViewModel: ReviewViewModel = viewModel()
        val selectedReviewState by reviewViewModel.selectedReview.collectAsState()

        LaunchedEffect(route.reviewId) {
            reviewViewModel.loadReview(route.reviewId)
        }

        when (val state = selectedReviewState) {
            is ReviewDetailsUiState.Loading -> LoadingContent()
            is ReviewDetailsUiState.Error -> ErrorContent(message = state.message)
            is ReviewDetailsUiState.Success -> ReviewScreen(review = state.review)
        }
    }
}
