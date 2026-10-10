package com.mobilelens.mobilelens.reviews.viewmodel

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.reviews.data.ReviewRepository
import com.mobilelens.mobilelens.reviews.model.ReviewComment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "CommentsViewModel"

/** The longest comment the backend accepts. */
const val COMMENT_MAX_LENGTH = 2000

sealed interface CommentsUiState {
    object Loading : CommentsUiState
    data class Success(
        val comments: List<ReviewComment>,
        // Comments whose deletion is on its way to the server
        val deletingIds: Set<String> = emptySet(),
    ) : CommentsUiState
    data class Error(@StringRes val messageRes: Int) : CommentsUiState
}

/** The comments under one published review, plus the comment being written. */
class CommentsViewModel(
    private val reviewRepository: ReviewRepository = ReviewRepository()
) : ViewModel() {
    private val _comments = MutableStateFlow<CommentsUiState>(CommentsUiState.Loading)
    val comments: StateFlow<CommentsUiState> = _comments.asStateFlow()

    /** The comment being written; kept here so it survives rotation and leaving the screen. */
    var draft by mutableStateOf("")
        private set

    private val _isPosting = MutableStateFlow(false)
    val isPosting: StateFlow<Boolean> = _isPosting.asStateFlow()

    // Outcome of a failed post or delete, shown once in a snackbar; cleared with messageShown()
    private val _message = MutableStateFlow<Int?>(null)
    val message: StateFlow<Int?> = _message.asStateFlow()

    fun updateDraft(text: String) {
        draft = text.take(COMMENT_MAX_LENGTH)
    }

    /** Loads the comments, or refreshes them in place when they are already showing. */
    fun loadComments(reviewId: String) {
        viewModelScope.launch {
            if (_comments.value !is CommentsUiState.Success) {
                _comments.value = CommentsUiState.Loading
            }
            try {
                val comments = reviewRepository.getComments(reviewId)
                _comments.update { state ->
                    val deletingIds = (state as? CommentsUiState.Success)?.deletingIds.orEmpty()
                    CommentsUiState.Success(comments, deletingIds)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load comments of review $reviewId", e)
                if (_comments.value !is CommentsUiState.Success) {
                    _comments.value = CommentsUiState.Error(R.string.error_load_comments)
                }
            }
        }
    }

    fun postComment(reviewId: String) {
        val text = draft.trim()
        if (text.isEmpty() || _isPosting.value || _comments.value !is CommentsUiState.Success) return
        _isPosting.value = true
        viewModelScope.launch {
            try {
                val comment = reviewRepository.addComment(reviewId, text)
                _comments.update { current ->
                    if (current is CommentsUiState.Success) {
                        current.copy(comments = current.comments + comment)
                    } else {
                        current
                    }
                }
                draft = ""
            } catch (e: Exception) {
                Log.w(TAG, "Failed to post a comment on review $reviewId", e)
                // The draft stays, so nothing that was typed is lost
                _message.value = R.string.error_post_comment
            } finally {
                _isPosting.value = false
            }
        }
    }

    fun deleteComment(reviewId: String, commentId: String) {
        val state = _comments.value
        if (state !is CommentsUiState.Success || commentId in state.deletingIds) return
        _comments.value = state.copy(deletingIds = state.deletingIds + commentId)
        viewModelScope.launch {
            val deleted = try {
                reviewRepository.deleteComment(reviewId, commentId)
                true
            } catch (e: Exception) {
                Log.w(TAG, "Failed to delete comment $commentId", e)
                false
            }
            _comments.update { current ->
                if (current !is CommentsUiState.Success) return@update current
                current.copy(
                    comments = if (deleted) current.comments.filterNot { it.id == commentId } else current.comments,
                    deletingIds = current.deletingIds - commentId,
                )
            }
            if (!deleted) _message.value = R.string.error_delete_comment
        }
    }

    fun messageShown() {
        _message.value = null
    }
}
