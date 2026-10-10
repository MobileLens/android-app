package com.mobilelens.mobilelens.reviews.viewmodel

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.reviews.data.ReviewRepository
import com.mobilelens.mobilelens.reviews.model.Review
import com.mobilelens.mobilelens.reviews.model.ReviewThread
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "ReviewViewModel"

sealed interface ReviewThreadUiState {
    object Loading : ReviewThreadUiState
    data class Success(val thread: ReviewThread) : ReviewThreadUiState
    data class Error(@StringRes val messageRes: Int) : ReviewThreadUiState
}

sealed interface ReviewDetailsUiState {
    object Loading : ReviewDetailsUiState
    data class Success(val review: Review) : ReviewDetailsUiState
    data class Error(@StringRes val messageRes: Int) : ReviewDetailsUiState
}

class ReviewViewModel(
    private val reviewRepository: ReviewRepository = ReviewRepository()
) : ViewModel() {
    private val _thread = MutableStateFlow<ReviewThreadUiState>(ReviewThreadUiState.Loading)
    val thread: StateFlow<ReviewThreadUiState> = _thread.asStateFlow()

    private val _selectedReview = MutableStateFlow<ReviewDetailsUiState>(ReviewDetailsUiState.Loading)
    val selectedReview: StateFlow<ReviewDetailsUiState> = _selectedReview.asStateFlow()

    // Outcome of a like that failed, shown once in a snackbar; cleared with messageShown()
    private val _message = MutableStateFlow<Int?>(null)
    val message: StateFlow<Int?> = _message.asStateFlow()

    private var likeInFlight = false
    private val threadLikesInFlight = mutableSetOf<String>()

    fun loadReviewsForPhone(phoneId: String) {
        viewModelScope.launch {
            _thread.value = ReviewThreadUiState.Loading
            try {
                val reviews = reviewRepository.getReviewsForPhone(phoneId)
                _thread.value = ReviewThreadUiState.Success(ReviewThread(id = phoneId, reviews = reviews))
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load reviews for phone $phoneId", e)
                _thread.value = ReviewThreadUiState.Error(R.string.error_load_reviews)
            }
        }
    }

    /**
     * Shows the review as last loaded at once, if it was, and refreshes it: its likes and
     * comments change under the cache, and `likedByMe` depends on who is signed in.
     */
    fun loadReview(reviewId: String) {
        viewModelScope.launch {
            val cached = reviewRepository.getCachedReview(reviewId)
            _selectedReview.value = if (cached != null) {
                ReviewDetailsUiState.Success(cached)
            } else {
                ReviewDetailsUiState.Loading
            }
            try {
                _selectedReview.value = ReviewDetailsUiState.Success(reviewRepository.getReview(reviewId))
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load review $reviewId", e)
                // A refresh that fails keeps the review it is already showing
                if (cached == null) {
                    _selectedReview.value = ReviewDetailsUiState.Error(R.string.error_load_review)
                }
            }
        }
    }

    /** Likes or unlikes the shown review. The heart flips at once and goes back if the call fails. */
    fun toggleLike() {
        val shown = (_selectedReview.value as? ReviewDetailsUiState.Success)?.review ?: return
        if (likeInFlight) return
        likeInFlight = true
        val wantLiked = !shown.likedByMe
        _selectedReview.value = ReviewDetailsUiState.Success(
            shown.copy(
                likedByMe = wantLiked,
                likeCount = (shown.likeCount + if (wantLiked) 1 else -1).coerceAtLeast(0),
            )
        )
        viewModelScope.launch {
            try {
                val response = reviewRepository.setLiked(shown.id, wantLiked)
                updateShown(shown.id) { it.copy(likedByMe = response.liked, likeCount = response.likeCount) }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to set like on review ${shown.id}", e)
                updateShown(shown.id) { it.copy(likedByMe = shown.likedByMe, likeCount = shown.likeCount) }
                _message.value = R.string.error_like_review
            } finally {
                likeInFlight = false
            }
        }
    }

    /** Likes or unlikes a review in the thread list, with the same revert on failure. */
    fun toggleThreadLike(reviewId: String) {
        val review = threadReview(reviewId) ?: return
        if (!threadLikesInFlight.add(reviewId)) return
        val wantLiked = !review.likedByMe
        updateThreadReview(reviewId) {
            it.copy(
                likedByMe = wantLiked,
                likeCount = (it.likeCount + if (wantLiked) 1 else -1).coerceAtLeast(0),
            )
        }
        viewModelScope.launch {
            try {
                val response = reviewRepository.setLiked(reviewId, wantLiked)
                updateThreadReview(reviewId) {
                    it.copy(likedByMe = response.liked, likeCount = response.likeCount)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to set like on review $reviewId", e)
                updateThreadReview(reviewId) {
                    it.copy(likedByMe = review.likedByMe, likeCount = review.likeCount)
                }
                _message.value = R.string.error_like_review
            } finally {
                threadLikesInFlight.remove(reviewId)
            }
        }
    }

    fun messageShown() {
        _message.value = null
    }

    private fun threadReview(reviewId: String): Review? =
        (_thread.value as? ReviewThreadUiState.Success)?.thread?.reviews?.firstOrNull { it.id == reviewId }

    private fun updateThreadReview(reviewId: String, transform: (Review) -> Review) {
        val current = _thread.value as? ReviewThreadUiState.Success ?: return
        _thread.value = ReviewThreadUiState.Success(
            current.thread.copy(
                reviews = current.thread.reviews?.map { if (it.id == reviewId) transform(it) else it }
            )
        )
    }

    private fun updateShown(reviewId: String, transform: (Review) -> Review) {
        val current = _selectedReview.value as? ReviewDetailsUiState.Success ?: return
        if (current.review.id != reviewId) return
        _selectedReview.value = ReviewDetailsUiState.Success(transform(current.review))
    }
}
