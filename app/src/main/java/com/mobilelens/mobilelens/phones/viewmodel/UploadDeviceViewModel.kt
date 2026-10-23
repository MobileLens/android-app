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
import com.mobilelens.mobilelens.phones.data.JPEG_MIME_TYPE
import com.mobilelens.mobilelens.phones.data.PhoneCatalogueRepository
import com.mobilelens.mobilelens.phones.data.PhotoExif
import com.mobilelens.mobilelens.phones.data.PhotoExifReader
import com.mobilelens.mobilelens.phones.data.PhotoLensMatch
import com.mobilelens.mobilelens.phones.data.checkPhotoLens
import com.mobilelens.mobilelens.phones.data.exifMatchesDevice
import com.mobilelens.mobilelens.phones.data.lensFromPhotoExif
import com.mobilelens.mobilelens.phones.data.sharesOpticsWith
import com.mobilelens.mobilelens.phones.model.DeviceInfo
import com.mobilelens.mobilelens.phones.model.Facing
import com.mobilelens.mobilelens.phones.model.GalleryCamera
import com.mobilelens.mobilelens.phones.model.Lens
import com.mobilelens.mobilelens.phones.model.LensType
import com.mobilelens.mobilelens.phones.model.Stabilization
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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

/** The Specs tab: this device's lenses, to send to its catalogue phone. */
sealed interface UploadDeviceUiState {
    object Loading : UploadDeviceUiState
    data class Ready(
        val deviceInfo: DeviceInfo,
        val lenses: List<Lens>,
        /** Indices of [lenses] already sent, which sending again skips. */
        val sentLensIndices: Set<Int> = emptySet(),
        /** Cameras added from photos, sent along with [lenses]. */
        val photoCameras: List<PhotoCamera> = emptyList(),
        /** Read from a photo, waiting for the user to fill in what EXIF doesn't record. */
        val pendingPhotoCamera: PhotoCamera? = null,
    ) : UploadDeviceUiState {
        /** Whether a lens or a camera from a photo is still to be sent. */
        val hasUnsentSpecs: Boolean
            get() = lenses.indices.any { it !in sentLensIndices } || photoCameras.any { !it.submitted }
    }
    data class Error(@StringRes val messageRes: Int) : UploadDeviceUiState
}

/** The Photos tab: the cameras of this device's catalogue phone, with photos picked for them. */
sealed interface UploadGalleryUiState {
    object Loading : UploadGalleryUiState
    data class Ready(
        /** Empty while the phone isn't in the catalogue, or has no cameras there yet. */
        val cameras: List<GalleryCamera>,
        /** Photos picked for each camera, by its id, waiting to be sent. */
        val photos: Map<String, List<Uri>> = emptyMap(),
    ) : UploadGalleryUiState {
        val hasPhotos: Boolean
            get() = photos.values.any { it.isNotEmpty() }
    }
    data class Error(@StringRes val messageRes: Int) : UploadGalleryUiState
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

    private val _gallery = MutableStateFlow<UploadGalleryUiState>(UploadGalleryUiState.Loading)
    val gallery: StateFlow<UploadGalleryUiState> = _gallery.asStateFlow()

    // Specs or photos on their way to the server; only one send runs at a time
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    // Outcome of a send or of reading a picked photo, shown once in a snackbar; cleared with messageShown()
    private val _message = MutableStateFlow<Int?>(null)
    val message: StateFlow<Int?> = _message.asStateFlow()

    // What earlier sends already sent, so sending again carries on from there: the backend takes
    // a repeated camera as a new submission rather than merging it
    private var submittedPhoneId: String? = null
    private val sentLensIndices = mutableSetOf<Int>()
    private val sentPhotoCameraIds = mutableSetOf<Int>()

    private var nextPhotoCameraId = 0
    private var galleryJob: Job? = null

    init {
        loadGallery()
    }

    /** Seeds from Home's Camera2 reading so every local lens is included. */
    fun prepare(lenses: List<Lens>, deviceInfo: DeviceInfo) {
        if (_uiState.value is UploadDeviceUiState.Ready) return
        _uiState.value = if (lenses.isEmpty()) {
            UploadDeviceUiState.Error(R.string.home_no_cameras)
        } else {
            UploadDeviceUiState.Ready(deviceInfo = deviceInfo, lenses = lenses)
        }
    }

    /** Reads a camera Camera2 missed from a stock-camera photo, then waits for [confirmPhotoCamera]. */
    fun readPhotoCamera(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            val exif = readExif(uri, contentResolver)
            val current = _uiState.value as? UploadDeviceUiState.Ready ?: return@launch
            val lens = exif?.let(::lensFromPhotoExif)
            val listed = current.lenses + current.photoCameras.map { it.lens }
            when {
                exif == null || !matchesThisDevice(exif) ->
                    _message.value = R.string.upload_error_photo_not_from_device
                lens == null -> _message.value = R.string.upload_error_photo_no_optics
                listed.any { it.sharesOpticsWith(lens) } ->
                    _message.value = R.string.upload_error_camera_already_listed
                else -> _uiState.value = current.copy(
                    pendingPhotoCamera = PhotoCamera(id = nextPhotoCameraId++, lens = lens),
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
        if (id in sentPhotoCameraIds) return
        _uiState.value = ready.copy(photoCameras = ready.photoCameras.filterNot { it.id == id })
    }

    /** Sends the lenses, and the cameras added from photos, that haven't been sent yet. */
    fun sendSpecs() {
        val ready = _uiState.value as? UploadDeviceUiState.Ready ?: return
        if (_isSubmitting.value) return

        viewModelScope.launch {
            _isSubmitting.value = true
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
                    if (index in sentLensIndices) return@forEachIndexed
                    withContext(Dispatchers.IO) { submitRepository.createCamera(phoneId, lens) }
                    sentLensIndices += index
                    _uiState.update { state ->
                        if (state !is UploadDeviceUiState.Ready) return@update state
                        state.copy(sentLensIndices = sentLensIndices.toSet())
                    }
                }
                ready.photoCameras.forEach { photoCamera ->
                    if (photoCamera.id in sentPhotoCameraIds) return@forEach
                    withContext(Dispatchers.IO) { submitRepository.createCamera(phoneId, photoCamera.lens) }
                    sentPhotoCameraIds += photoCamera.id
                    markPhotoCameraSent(photoCamera.id)
                }

                _message.value = R.string.upload_specs_sent
                // The new cameras can take photos straight away, while they're in review
                loadGallery()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Failed to send the device's camera specs", e)
                _message.value = submitErrorFor(e, stage)
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    /** Lists the cameras of this device's catalogue phone, which gallery photos are uploaded for. */
    fun loadGallery() {
        galleryJob?.cancel()
        galleryJob = viewModelScope.launch {
            if (_gallery.value !is UploadGalleryUiState.Ready) {
                _gallery.value = UploadGalleryUiState.Loading
            }
            try {
                val phoneId = submittedPhoneId
                    ?: phoneRepository.findPhoneForDevice(buildInfoRepository.getModel(), deviceMatchStore)?.id
                val cameras = phoneId?.let { submitRepository.getGalleryCameras(it) }.orEmpty()
                _gallery.update { state ->
                    // Photos already picked stay with their cameras
                    val picked = (state as? UploadGalleryUiState.Ready)?.photos.orEmpty()
                    UploadGalleryUiState.Ready(
                        cameras = cameras,
                        photos = picked.filterKeys { id -> cameras.any { it.id == id } },
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load the cameras to upload photos for", e)
                // A refresh that fails keeps the cameras already showing
                if (_gallery.value !is UploadGalleryUiState.Ready) {
                    _gallery.value = UploadGalleryUiState.Error(R.string.upload_error_load_cameras)
                }
            }
        }
    }

    /**
     * Adds photos taken on this phone with the camera [cameraId], as their EXIF tells. The others
     * are left out, and the reason the first of them was is shown.
     */
    fun addGalleryPhotos(cameraId: String, uris: List<Uri>, contentResolver: ContentResolver) {
        val camera = (_gallery.value as? UploadGalleryUiState.Ready)?.cameras
            ?.firstOrNull { it.id == cameraId }
            ?: return
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val problems = uris.associateWith { galleryPhotoProblem(it, camera, contentResolver) }
            problems.values.firstOrNull { it != null }?.let { _message.value = it }
            val accepted = problems.filterValues { it == null }.keys
            if (accepted.isEmpty()) return@launch
            _gallery.update { state ->
                if (state !is UploadGalleryUiState.Ready) return@update state
                val picked = (state.photos[cameraId].orEmpty() + accepted).distinct()
                state.copy(photos = state.photos + (cameraId to picked))
            }
        }
    }

    fun removeGalleryPhoto(cameraId: String, uri: Uri) {
        if (_isSubmitting.value) return
        dropGalleryPhoto(cameraId, uri)
    }

    /** Uploads the picked photos. Each one shows in the phone's gallery once it's been reviewed. */
    fun sendPhotos(contentResolver: ContentResolver) {
        val gallery = _gallery.value as? UploadGalleryUiState.Ready ?: return
        if (_isSubmitting.value || !gallery.hasPhotos) return

        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                gallery.photos.forEach { (cameraId, uris) ->
                    val camera = gallery.cameras.firstOrNull { it.id == cameraId } ?: return@forEach
                    uris.forEach { uri ->
                        uploadPhoto(camera, uri, contentResolver)
                        // Off the list once sent, so sending again after a failure only sends the rest
                        dropGalleryPhoto(cameraId, uri)
                    }
                }
                _message.value = R.string.upload_photos_sent
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Failed to upload gallery photos", e)
                _message.value = submitErrorFor(e, SubmitStage.PHOTOS)
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun messageShown() {
        _message.value = null
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

    // Why a photo can't go up for [camera], as the string resource of the message saying it was
    // left out; null when it can
    private suspend fun galleryPhotoProblem(
        uri: Uri,
        camera: GalleryCamera,
        contentResolver: ContentResolver,
    ): Int? {
        // Only a JPEG's metadata can be removed without re-encoding the photo
        if (contentResolver.getType(uri) != JPEG_MIME_TYPE) return R.string.upload_error_photos_not_jpeg
        val exif = readExif(uri, contentResolver)
        if (exif == null || !matchesThisDevice(exif)) return R.string.upload_error_photos_other_phone
        return when (checkPhotoLens(exif, camera)) {
            PhotoLensMatch.MATCHES -> null
            PhotoLensMatch.NO_LENS_DATA -> R.string.upload_error_photos_no_lens_data
            PhotoLensMatch.OTHER_LENS -> R.string.upload_error_photos_other_lens
        }
    }

    private suspend fun uploadPhoto(camera: GalleryCamera, uri: Uri, contentResolver: ContentResolver) {
        withContext(Dispatchers.IO) {
            val jpeg = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw FileNotFoundException("Couldn't open $uri")
            // Checked when the photo was picked too, but the file may have changed since
            val exif = photoExifReader.read(contentResolver, uri)
            check(matchesThisDevice(exif)) { "EXIF no longer matches this device" }
            check(checkPhotoLens(exif, camera) == PhotoLensMatch.MATCHES) {
                "EXIF no longer matches camera ${camera.id}"
            }
            submitRepository.uploadLensPhoto(camera.id, jpeg, exif)
        }
    }

    private fun dropGalleryPhoto(cameraId: String, uri: Uri) {
        _gallery.update { state ->
            if (state !is UploadGalleryUiState.Ready) return@update state
            state.copy(photos = state.photos + (cameraId to (state.photos[cameraId].orEmpty() - uri)))
        }
    }

    private fun markPhotoCameraSent(photoCameraId: Int) {
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
