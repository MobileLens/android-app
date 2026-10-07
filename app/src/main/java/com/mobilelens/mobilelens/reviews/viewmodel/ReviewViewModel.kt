package com.mobilelens.mobilelens.reviews.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilelens.mobilelens.reviews.data.ReviewRepository
import com.mobilelens.mobilelens.reviews.model.Review
import com.mobilelens.mobilelens.reviews.model.ReviewThread
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ReviewThreadUiState {
    object Loading : ReviewThreadUiState
    data class Success(val thread: ReviewThread) : ReviewThreadUiState
    data class Error(val message: String) : ReviewThreadUiState
}

sealed interface ReviewDetailsUiState {
    object Loading : ReviewDetailsUiState
    data class Success(val review: Review) : ReviewDetailsUiState
    data class Error(val message: String) : ReviewDetailsUiState
}

class ReviewViewModel(
    private val reviewRepository: ReviewRepository = ReviewRepository()
) : ViewModel() {
    private val _thread = MutableStateFlow<ReviewThreadUiState>(ReviewThreadUiState.Loading)
    val thread: StateFlow<ReviewThreadUiState> = _thread.asStateFlow()

    private val _selectedReview = MutableStateFlow<ReviewDetailsUiState>(ReviewDetailsUiState.Loading)
    val selectedReview: StateFlow<ReviewDetailsUiState> = _selectedReview.asStateFlow()

    fun loadReviewsForPhone(phoneId: String) {
        viewModelScope.launch {
            _thread.value = ReviewThreadUiState.Loading
            try {
                val reviews = reviewRepository.getReviewsForPhone(phoneId)
                _thread.value = ReviewThreadUiState.Success(ReviewThread(id = phoneId, reviews = reviews))
            } catch (e: Exception) {
                _thread.value = ReviewThreadUiState.Error(e.message ?: "Failed to load reviews.")
            }
        }
    }

    fun loadReview(reviewId: String) {
        viewModelScope.launch {
            _selectedReview.value = ReviewDetailsUiState.Loading
            try {
                _selectedReview.value = ReviewDetailsUiState.Success(reviewRepository.getReview(reviewId))
            } catch (e: Exception) {
                _selectedReview.value = ReviewDetailsUiState.Error(e.message ?: "Failed to load review.")
            }
        }
    }
}
