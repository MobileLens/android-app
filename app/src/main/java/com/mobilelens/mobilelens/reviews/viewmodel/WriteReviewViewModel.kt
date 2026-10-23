package com.mobilelens.mobilelens.reviews.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.data.BuildInfoRepository
import com.mobilelens.mobilelens.core.data.remote.dtos.StorageUploadResponse
import com.mobilelens.mobilelens.core.data.remote.publicMediaUrl
import com.mobilelens.mobilelens.phones.data.DeviceMatchStore
import com.mobilelens.mobilelens.phones.data.PhoneCatalogueRepository
import com.mobilelens.mobilelens.phones.model.Phone
import com.mobilelens.mobilelens.reviews.data.ReviewRepository
import com.mobilelens.mobilelens.reviews.ui.insertMarkdownImage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
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

class WriteReviewViewModel(application: Application) : AndroidViewModel(application) {
    private val reviewRepository = ReviewRepository()
    private val phoneRepository = PhoneCatalogueRepository()
    private val buildInfoRepository = BuildInfoRepository()
    private val deviceMatchStore = DeviceMatchStore.getInstance(application)

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

    /**
     * [phoneId] is a catalogue phone, or null for this device: the phone the user picked for it
     * earlier, otherwise the catalogue phone whose model name is Build.MODEL.
     */
    fun loadTarget(phoneId: String?) {
        // Composition re-runs this after configuration changes and on returning from the picker;
        // after the first load the target only changes through choosePhone
        if (loadTargetJob != null) return

        lookUpTarget {
            if (phoneId != null) {
                phoneRepository.getPhoneById(phoneId)
            } else {
                findPhoneForDevice()
            }
        }
    }

    /** Reviews [phoneId] instead, picked by the user for this device, and remembers the choice. */
    fun choosePhone(phoneId: String) {
        if ((_target.value as? ReviewTargetUiState.Found)?.phone?.id == phoneId) return

        lookUpTarget {
            phoneRepository.getPhoneById(phoneId).also {
                deviceMatchStore.setPhoneId(buildInfoRepository.getModel(), phoneId)
            }
        }
    }

    private fun lookUpTarget(findPhone: suspend () -> Phone?) {
        // Only the newest lookup may update the target
        loadTargetJob?.cancel()
        loadTargetJob = viewModelScope.launch {
            _target.value = ReviewTargetUiState.Loading
            _target.value = try {
                val phone = findPhone()
                if (phone != null) {
                    ReviewTargetUiState.Found(phone)
                } else {
                    ReviewTargetUiState.Error(R.string.review_editor_device_not_in_catalogue)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load the phone to review", e)
                ReviewTargetUiState.Error(R.string.error_load_phone)
            }
        }
    }

    private suspend fun findPhoneForDevice(): Phone? {
        val model = buildInfoRepository.getModel()
        deviceMatchStore.getPhoneId(model)?.let { pickedId ->
            try {
                return phoneRepository.getPhoneById(pickedId)
            } catch (e: HttpException) {
                if (e.code() != 404) throw e
                // The picked phone was removed from the catalogue; match by model again
                deviceMatchStore.clear()
            }
        }
        return phoneRepository.findPhoneByModel(model)
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
