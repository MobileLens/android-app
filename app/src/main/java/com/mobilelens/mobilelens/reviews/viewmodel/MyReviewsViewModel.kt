package com.mobilelens.mobilelens.reviews.viewmodel

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.reviews.data.ReviewRepository
import com.mobilelens.mobilelens.reviews.model.Review
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "MyReviewsViewModel"

sealed interface MyReviewsUiState {
    object Loading : MyReviewsUiState
    data class Success(
        val reviews: List<Review>,
        // Reviews whose deletion is on its way to the server
        val deletingIds: Set<String> = emptySet(),
    ) : MyReviewsUiState
    data class Error(@StringRes val messageRes: Int) : MyReviewsUiState
}

/** The signed-in user's own reviews, whatever their status, as listed on the account screen. */
class MyReviewsViewModel(
    private val reviewRepository: ReviewRepository = ReviewRepository()
) : ViewModel() {
    private val _reviews = MutableStateFlow<MyReviewsUiState>(MyReviewsUiState.Loading)
    val reviews: StateFlow<MyReviewsUiState> = _reviews.asStateFlow()

    // Outcome of a deletion, shown once in a snackbar; cleared with messageShown()
    private val _message = MutableStateFlow<Int?>(null)
    val message: StateFlow<Int?> = _message.asStateFlow()

    /** Loads the list, or refreshes it in place when it's already showing. */
    fun loadReviews() {
        viewModelScope.launch {
            if (_reviews.value !is MyReviewsUiState.Success) {
                _reviews.value = MyReviewsUiState.Loading
            }
            try {
                val reviews = reviewRepository.getMyReviews()
                _reviews.update { state ->
                    val deletingIds = (state as? MyReviewsUiState.Success)?.deletingIds.orEmpty()
                    MyReviewsUiState.Success(reviews, deletingIds)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load the user's reviews", e)
                // A refresh that fails keeps the list it already has
                if (_reviews.value !is MyReviewsUiState.Success) {
                    _reviews.value = MyReviewsUiState.Error(R.string.error_load_reviews)
                }
            }
        }
    }

    fun deleteReview(reviewId: String) {
        val state = _reviews.value
        if (state !is MyReviewsUiState.Success || reviewId in state.deletingIds) return
        _reviews.value = state.copy(deletingIds = state.deletingIds + reviewId)
        viewModelScope.launch {
            val deleted = try {
                reviewRepository.deleteReview(reviewId)
                true
            } catch (e: Exception) {
                Log.w(TAG, "Failed to delete review $reviewId", e)
                false
            }
            _reviews.update { current ->
                if (current !is MyReviewsUiState.Success) return@update current
                current.copy(
                    reviews = if (deleted) current.reviews.filterNot { it.id == reviewId } else current.reviews,
                    deletingIds = current.deletingIds - reviewId,
                )
            }
            _message.value = if (deleted) R.string.settings_review_deleted else R.string.settings_error_delete_review
        }
    }

    fun messageShown() {
        _message.value = null
    }
}
