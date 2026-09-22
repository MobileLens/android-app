package com.mobilelens.mobilelens.reviews.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilelens.mobilelens.core.data.remote.ApiClient
import com.mobilelens.mobilelens.phones.data.PhoneCatalogueRepository
import com.mobilelens.mobilelens.reviews.data.remote.ReviewApi
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

class ReviewViewModel : ViewModel() {
    private val reviewApi: ReviewApi = ApiClient.createService()
    private val phoneCatalogueRepository = PhoneCatalogueRepository()

    private val _thread = MutableStateFlow<ReviewThreadUiState>(ReviewThreadUiState.Loading)
    val thread: StateFlow<ReviewThreadUiState> = _thread.asStateFlow()
    
    private val _selectedReview = MutableStateFlow<ReviewDetailsUiState>(ReviewDetailsUiState.Loading)
    val selectedReview: StateFlow<ReviewDetailsUiState> = _selectedReview.asStateFlow()

    fun loadReviewsForPhone(phoneId: String) {
        viewModelScope.launch {
            _thread.value = ReviewThreadUiState.Loading
            try {
                // Find phone via api/repository
                val phone = phoneCatalogueRepository.getPhoneById(phoneId)

                val dtos = reviewApi.getReviews(phoneId)
                val reviews = dtos.map { dto ->
                    Review(
                        id = dto.id,
                        title = dto.title,
                        author = dto.authorId,
                        content = dto.contentMarkdown,
                        createdAt = dto.createdAt,
                        updatedAt = dto.updatedAt,
                        commentCount = 0,
                        likeCount = 0,
                        assets = emptyList()
                    )
                }
                _thread.value = ReviewThreadUiState.Success(ReviewThread(id = phoneId, phone = phone, reviews = reviews))
            } catch (e: Exception) {
                _thread.value = ReviewThreadUiState.Error(e.message ?: "Failed to load reviews.")
            }
        }
    }
    
    fun loadReview(reviewId: String) {
        viewModelScope.launch {
            _selectedReview.value = ReviewDetailsUiState.Loading
            try {
                val currentState = _thread.value
                if (currentState is ReviewThreadUiState.Success) {
                    val existingReview = currentState.thread.reviews?.find { it.id == reviewId }
                    if (existingReview != null) {
                        _selectedReview.value = ReviewDetailsUiState.Success(existingReview)
                        return@launch
                    }
                }
                
                val dto = reviewApi.getReview(reviewId)
                _selectedReview.value = ReviewDetailsUiState.Success(
                    Review(
                        id = dto.id,
                        title = dto.title,
                        author = dto.authorId,
                        content = dto.contentMarkdown,
                        createdAt = dto.createdAt,
                        updatedAt = dto.updatedAt,
                        commentCount = 0,
                        likeCount = 0,
                        assets = emptyList()
                    )
                )
            } catch (e: Exception) {
                _selectedReview.value = ReviewDetailsUiState.Error(e.message ?: "Failed to load review.")
            }
        }
    }
}
