package com.mobilelens.mobilelens.reviews.viewmodel

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.data.BuildInfoRepository
import com.mobilelens.mobilelens.core.data.remote.dtos.StorageUploadResponse
import com.mobilelens.mobilelens.core.data.remote.publicMediaUrl
import com.mobilelens.mobilelens.phones.data.PhoneCatalogueRepository
import com.mobilelens.mobilelens.phones.model.Phone
import com.mobilelens.mobilelens.reviews.data.ReviewRepository
import com.mobilelens.mobilelens.reviews.ui.insertMarkdownImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException

private const val TAG = "WriteReviewViewModel"

/** The phone being reviewed. */
sealed interface ReviewTargetUiState {
    object Loading : ReviewTargetUiState
    data class Found(val phone: Phone) : ReviewTargetUiState
    data class Error(@StringRes val messageRes: Int) : ReviewTargetUiState
}

sealed interface PublishReviewUiState {
    object Idle : PublishReviewUiState
    object Publishing : PublishReviewUiState
    // inModeration: a regular user's review waits for a moderator, reviewers and above go live at once
    data class Published(val inModeration: Boolean) : PublishReviewUiState
}

// Address the editor shows the image under: the signed link works while the file is still private
private val StorageUploadResponse.previewUrl: String
    get() = url ?: storageUrl

class WriteReviewViewModel(
    private val reviewRepository: ReviewRepository = ReviewRepository(),
    private val phoneRepository: PhoneCatalogueRepository = PhoneCatalogueRepository(),
    private val buildInfoRepository: BuildInfoRepository = BuildInfoRepository()
) : ViewModel() {
    // Held here rather than in composition so the draft survives configuration changes
    val title = TextFieldState()
    val content = TextFieldState()

    private val _target = MutableStateFlow<ReviewTargetUiState>(ReviewTargetUiState.Loading)
    val target: StateFlow<ReviewTargetUiState> = _target.asStateFlow()

    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage.asStateFlow()

    private val _publishState = MutableStateFlow<PublishReviewUiState>(PublishReviewUiState.Idle)
    val publishState: StateFlow<PublishReviewUiState> = _publishState.asStateFlow()

    // Failed uploads/publishes, shown once in a snackbar; cleared with errorMessageShown()
    private val _errorMessage = MutableStateFlow<Int?>(null)
    val errorMessage: StateFlow<Int?> = _errorMessage.asStateFlow()

    private val uploadedImages = mutableListOf<StorageUploadResponse>()
    private var loadTargetJob: Job? = null

    /** [phoneId] is a catalogue phone, or null for this device, matched to the catalogue by model. */
    fun loadTarget(phoneId: String?) {
        // Composition re-runs this after configuration changes; keep what is already loaded
        if (loadTargetJob?.isActive == true || _target.value is ReviewTargetUiState.Found) return

        loadTargetJob = viewModelScope.launch {
            _target.value = ReviewTargetUiState.Loading
            _target.value = try {
                val phone = if (phoneId != null) {
                    phoneRepository.getPhoneById(phoneId)
                } else {
                    phoneRepository.findPhoneByModel(buildInfoRepository.getModel())
                }
                if (phone != null) {
                    ReviewTargetUiState.Found(phone)
                } else {
                    ReviewTargetUiState.Error(R.string.review_editor_device_not_in_catalogue)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load phone ${phoneId ?: "for this device"}", e)
                ReviewTargetUiState.Error(R.string.error_load_phone)
            }
        }
    }

    /** Uploads the picked image and inserts it into the content at the cursor. */
    fun insertImage(contentResolver: ContentResolver, uri: Uri) {
        if (_isUploadingImage.value) return

        viewModelScope.launch {
            _isUploadingImage.value = true
            try {
                val upload = withContext(Dispatchers.IO) {
                    val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: throw FileNotFoundException("Couldn't open $uri")
                    reviewRepository.uploadReviewImage(bytes, mimeType)
                }
                uploadedImages += upload
                content.insertMarkdownImage(upload.previewUrl)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to upload review image $uri", e)
                _errorMessage.value = R.string.error_upload_review_image
            } finally {
                _isUploadingImage.value = false
            }
        }
    }

    fun publish() {
        val phone = (_target.value as? ReviewTargetUiState.Found)?.phone ?: return
        if (_publishState.value != PublishReviewUiState.Idle) return

        val draft = content.text.toString()
        // Images deleted from the text since uploading aren't attached to the review.
        val images = uploadedImages.filter { it.previewUrl in draft }
        // The preview links are signed and expire, so the saved text points at the permanent public
        // URLs instead. The backend moves the files there once the review is published.
        val markdown = images.fold(draft) { text, image ->
            val publicUrl = publicMediaUrl(image.storageUrl)
            if (publicUrl != null) text.replace(image.previewUrl, publicUrl) else text
        }

        viewModelScope.launch {
            _publishState.value = PublishReviewUiState.Publishing
            try {
                val review = reviewRepository.createReview(
                    phoneId = phone.id,
                    title = title.text.toString().trim(),
                    contentMarkdown = markdown,
                    images = images
                )
                _publishState.value = PublishReviewUiState.Published(
                    inModeration = review.status != "published"
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed to publish review for phone ${phone.id}", e)
                _errorMessage.value = R.string.error_publish_review
                _publishState.value = PublishReviewUiState.Idle
            }
        }
    }

    fun errorMessageShown() {
        _errorMessage.value = null
    }
}
