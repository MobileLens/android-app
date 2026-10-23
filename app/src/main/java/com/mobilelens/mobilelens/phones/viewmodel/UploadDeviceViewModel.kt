package com.mobilelens.mobilelens.phones.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.data.BuildInfoRepository
import com.mobilelens.mobilelens.core.data.remote.apiErrorCode
import com.mobilelens.mobilelens.phones.data.CameraSubmitRepository
import com.mobilelens.mobilelens.phones.data.DeviceMatchStore
import com.mobilelens.mobilelens.phones.data.PhoneCatalogueRepository
import com.mobilelens.mobilelens.phones.data.PhotoExif
import com.mobilelens.mobilelens.phones.data.PhotoExifReader
import com.mobilelens.mobilelens.phones.data.exifMatchesDevice
import com.mobilelens.mobilelens.phones.data.lensFromPhotoExif
import com.mobilelens.mobilelens.phones.data.sharesOpticsWith
import com.mobilelens.mobilelens.phones.model.DeviceInfo
import com.mobilelens.mobilelens.phones.model.Facing
import com.mobilelens.mobilelens.phones.model.Lens
import com.mobilelens.mobilelens.phones.model.LensType
import com.mobilelens.mobilelens.phones.model.Stabilization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.FileNotFoundException

private const val TAG = "UploadDeviceViewModel"

/**
 * A camera Camera2 doesn't expose, described by the EXIF of a stock-camera photo taken with it.
 * Only the lens is sent, like a detected one: the photo stays on the phone.
 */
data class PhotoCamera(
    val id: Int,
    val lens: Lens,
    /** Already created on the backend, where removing it here would no longer withdraw it. */
    val submitted: Boolean = false,
)

sealed interface UploadDeviceUiState {
    object Loading : UploadDeviceUiState
    data class Ready(
        val deviceInfo: DeviceInfo,
        val lenses: List<Lens>,
        /** Optional full-res stock photo URI per lens index. */
        val photoUris: Map<Int, Uri> = emptyMap(),
        /** Cameras added from photos, sent along with [lenses]. */
        val photoCameras: List<PhotoCamera> = emptyList(),
        /** Read from a photo, waiting for the user to fill in what EXIF doesn't record. */
        val pendingPhotoCamera: PhotoCamera? = null,
        @StringRes val photoErrorRes: Int? = null,
    ) : UploadDeviceUiState
    data class Error(@StringRes val messageRes: Int) : UploadDeviceUiState
}

sealed interface UploadSubmitState {
    object Idle : UploadSubmitState
    object Submitting : UploadSubmitState
    object Succeeded : UploadSubmitState
    data class Failed(@StringRes val messageRes: Int) : UploadSubmitState
}

private enum class SubmitStage { DEVICE, CAMERAS, PHOTOS }

/**
 * A device that isn't listed yet is added to the catalogue in the DEVICE stage. Any signed-in user
 * may do that, but below moderator only a few phones a day (429 PHONE_LIMIT_REACHED).
 */
@StringRes
private fun submitErrorFor(e: Exception, stage: SubmitStage): Int {
    if (e !is HttpException) return R.string.upload_error_submit
    return when {
        e.apiErrorCode == "USER_BANNED" -> R.string.upload_error_banned
        // The session ended since the upload screen was opened
        e.code() == 401 -> R.string.upload_login_required_message
        stage == SubmitStage.DEVICE && e.apiErrorCode == "PHONE_LIMIT_REACHED" -> R.string.upload_error_phone_limit
        stage == SubmitStage.CAMERAS && e.apiErrorCode == "INVALID_FIELD" -> R.string.upload_error_invalid_camera
        else -> R.string.upload_error_submit
    }
}

class UploadDeviceViewModel(application: Application) : AndroidViewModel(application) {
    private val phoneRepository = PhoneCatalogueRepository()
    private val submitRepository = CameraSubmitRepository()
    private val buildInfoRepository = BuildInfoRepository()
    private val photoExifReader = PhotoExifReader()
    // The catalogue phone the user picked for this device when writing a review
    private val deviceMatchStore = DeviceMatchStore.getInstance(application)

    private val _uiState = MutableStateFlow<UploadDeviceUiState>(UploadDeviceUiState.Loading)
    val uiState: StateFlow<UploadDeviceUiState> = _uiState.asStateFlow()

    private val _submitState = MutableStateFlow<UploadSubmitState>(UploadSubmitState.Idle)
    val submitState: StateFlow<UploadSubmitState> = _submitState.asStateFlow()

    // What earlier attempts already sent, so a retry after a failure carries on from there: the
    // backend takes a repeated camera as a new submission rather than merging it
    private var submittedPhoneId: String? = null
    private val createdCameraIds = mutableMapOf<Int, String>() // lens index -> camera id
    private val uploadedPhotos = mutableMapOf<Int, Uri>() // lens index -> photo sent for it
    private val createdPhotoCameraIds = mutableMapOf<Int, String>() // photo camera id -> camera id

    private var nextPhotoCameraId = 0

    /** Seeds from Home's Camera2 reading so every local lens is included. */
    fun prepare(lenses: List<Lens>, deviceInfo: DeviceInfo) {
        if (_uiState.value is UploadDeviceUiState.Ready) return
        _uiState.value = if (lenses.isEmpty()) {
            UploadDeviceUiState.Error(R.string.home_no_cameras)
        } else {
            UploadDeviceUiState.Ready(deviceInfo = deviceInfo, lenses = lenses)
        }
    }

    fun setPhoto(lensIndex: Int, uri: Uri?, contentResolver: ContentResolver) {
        val ready = _uiState.value as? UploadDeviceUiState.Ready ?: return
        if (uri == null) {
            _uiState.value = ready.copy(
                photoUris = ready.photoUris - lensIndex,
                photoErrorRes = null,
            )
            return
        }

        viewModelScope.launch {
            val matches = readExif(uri, contentResolver)?.let(::matchesThisDevice) == true
            val current = _uiState.value as? UploadDeviceUiState.Ready ?: return@launch
            _uiState.value = if (matches) {
                current.copy(
                    photoUris = current.photoUris + (lensIndex to uri),
                    photoErrorRes = null,
                )
            } else {
                current.copy(photoErrorRes = R.string.upload_error_photo_not_from_device)
            }
        }
    }

    /** Reads a camera Camera2 missed from a stock-camera photo, then waits for [confirmPhotoCamera]. */
    fun readPhotoCamera(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            val exif = readExif(uri, contentResolver)
            val current = _uiState.value as? UploadDeviceUiState.Ready ?: return@launch
            val lens = exif?.let(::lensFromPhotoExif)
            val listed = current.lenses + current.photoCameras.map { it.lens }
            _uiState.value = when {
                exif == null || !matchesThisDevice(exif) ->
                    current.copy(photoErrorRes = R.string.upload_error_photo_not_from_device)
                lens == null -> current.copy(photoErrorRes = R.string.upload_error_photo_no_optics)
                listed.any { it.sharesOpticsWith(lens) } ->
                    current.copy(photoErrorRes = R.string.upload_error_camera_already_listed)
                else -> current.copy(
                    pendingPhotoCamera = PhotoCamera(id = nextPhotoCameraId++, lens = lens),
                    photoErrorRes = null,
                )
            }
        }
    }

    fun confirmPhotoCamera(type: LensType, facing: Facing, stabilization: Stabilization) {
        val ready = _uiState.value as? UploadDeviceUiState.Ready ?: return
        val pending = ready.pendingPhotoCamera ?: return
        val confirmed = pending.copy(
            lens = pending.lens.copy(type = type, facing = facing, stabilization = stabilization)
        )
        _uiState.value = ready.copy(
            photoCameras = ready.photoCameras + confirmed,
            pendingPhotoCamera = null,
        )
    }

    fun dismissPhotoCamera() {
        val ready = _uiState.value as? UploadDeviceUiState.Ready ?: return
        _uiState.value = ready.copy(pendingPhotoCamera = null)
    }

    fun removePhotoCamera(id: Int) {
        val ready = _uiState.value as? UploadDeviceUiState.Ready ?: return
        if (id in createdPhotoCameraIds) return
        _uiState.value = ready.copy(photoCameras = ready.photoCameras.filterNot { it.id == id })
    }

    fun clearPhotoError() {
        val ready = _uiState.value as? UploadDeviceUiState.Ready ?: return
        if (ready.photoErrorRes != null) {
            _uiState.value = ready.copy(photoErrorRes = null)
        }
    }

    fun submit(contentResolver: ContentResolver) {
        val ready = _uiState.value as? UploadDeviceUiState.Ready ?: return
        if (_submitState.value == UploadSubmitState.Submitting) return

        viewModelScope.launch {
            _submitState.value = UploadSubmitState.Submitting
            var stage = SubmitStage.DEVICE
            try {
                val phoneId = submittedPhoneId ?: withContext(Dispatchers.IO) {
                    phoneRepository.findOrCreatePhoneForDevice(
                        model = buildInfoRepository.getModel(),
                        brandName = buildInfoRepository.getBrand(),
                        matchStore = deviceMatchStore,
                    ).id
                }.also { submittedPhoneId = it }

                stage = SubmitStage.CAMERAS
                ready.lenses.forEachIndexed { index, lens ->
                    if (index in createdCameraIds) return@forEachIndexed
                    val created = withContext(Dispatchers.IO) {
                        submitRepository.createCamera(phoneId, lens)
                    }
                    createdCameraIds[index] = created.id
                }
                ready.photoCameras.forEach { photoCamera ->
                    if (photoCamera.id in createdPhotoCameraIds) return@forEach
                    val created = withContext(Dispatchers.IO) {
                        submitRepository.createCamera(phoneId, photoCamera.lens)
                    }
                    createdPhotoCameraIds[photoCamera.id] = created.id
                    markSubmitted(photoCamera.id)
                }

                stage = SubmitStage.PHOTOS
                ready.photoUris.forEach { (index, uri) ->
                    // A photo picked in place of one that was already sent goes up as well
                    if (uploadedPhotos[index] == uri) return@forEach
                    val cameraId = createdCameraIds[index] ?: return@forEach
                    uploadPhoto(cameraId, uri, contentResolver)
                    uploadedPhotos[index] = uri
                }

                _submitState.value = UploadSubmitState.Succeeded
            } catch (e: Exception) {
                Log.w(TAG, "Failed to upload device cameras", e)
                _submitState.value = UploadSubmitState.Failed(submitErrorFor(e, stage))
            }
        }
    }

    fun submitMessageShown() {
        if (_submitState.value is UploadSubmitState.Failed) {
            _submitState.value = UploadSubmitState.Idle
        }
    }

    private suspend fun readExif(uri: Uri, contentResolver: ContentResolver): PhotoExif? =
        withContext(Dispatchers.IO) {
            try {
                photoExifReader.read(contentResolver, uri)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to read EXIF for $uri", e)
                null
            }
        }

    private fun matchesThisDevice(exif: PhotoExif): Boolean = exifMatchesDevice(
        exifMake = exif.make,
        exifModel = exif.model,
        manufacturer = buildInfoRepository.getBrand(),
        brand = Build.BRAND,
        model = buildInfoRepository.getModel(),
    )

    private suspend fun uploadPhoto(cameraId: String, uri: Uri, contentResolver: ContentResolver) {
        withContext(Dispatchers.IO) {
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw FileNotFoundException("Couldn't open $uri")
            val exif = photoExifReader.read(contentResolver, uri)
            check(matchesThisDevice(exif)) { "EXIF no longer matches this device" }
            submitRepository.uploadLensPhoto(cameraId, bytes, mimeType, exif)
        }
    }

    private fun markSubmitted(photoCameraId: Int) {
        _uiState.update { state ->
            if (state !is UploadDeviceUiState.Ready) return@update state
            state.copy(
                photoCameras = state.photoCameras.map {
                    if (it.id == photoCameraId) it.copy(submitted = true) else it
                }
            )
        }
    }
}
