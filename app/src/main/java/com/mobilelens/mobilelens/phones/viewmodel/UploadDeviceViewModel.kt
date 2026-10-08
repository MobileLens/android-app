package com.mobilelens.mobilelens.phones.viewmodel

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.data.BuildInfoRepository
import com.mobilelens.mobilelens.phones.data.CameraSubmitRepository
import com.mobilelens.mobilelens.phones.data.PhoneCatalogueRepository
import com.mobilelens.mobilelens.phones.data.PhotoExifReader
import com.mobilelens.mobilelens.phones.data.exifMatchesDevice
import com.mobilelens.mobilelens.phones.model.DeviceInfo
import com.mobilelens.mobilelens.phones.model.Lens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException

private const val TAG = "UploadDeviceViewModel"

sealed interface UploadDeviceUiState {
    object Loading : UploadDeviceUiState
    data class Ready(
        val deviceInfo: DeviceInfo,
        val lenses: List<Lens>,
        /** Optional full-res stock photo URI per lens index. */
        val photoUris: Map<Int, Uri> = emptyMap(),
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

class UploadDeviceViewModel(
    private val phoneRepository: PhoneCatalogueRepository = PhoneCatalogueRepository(),
    private val submitRepository: CameraSubmitRepository = CameraSubmitRepository(),
    private val buildInfoRepository: BuildInfoRepository = BuildInfoRepository(),
    private val photoExifReader: PhotoExifReader = PhotoExifReader(),
) : ViewModel() {
    private val _uiState = MutableStateFlow<UploadDeviceUiState>(UploadDeviceUiState.Loading)
    val uiState: StateFlow<UploadDeviceUiState> = _uiState.asStateFlow()

    private val _submitState = MutableStateFlow<UploadSubmitState>(UploadSubmitState.Idle)
    val submitState: StateFlow<UploadSubmitState> = _submitState.asStateFlow()

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
            val matches = withContext(Dispatchers.IO) {
                try {
                    val exif = photoExifReader.read(contentResolver, uri)
                    exifMatchesDevice(
                        exifMake = exif.make,
                        exifModel = exif.model,
                        manufacturer = buildInfoRepository.getBrand(),
                        brand = Build.BRAND,
                        model = buildInfoRepository.getModel(),
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to read EXIF for $uri", e)
                    false
                }
            }
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
            try {
                val phone = withContext(Dispatchers.IO) {
                    phoneRepository.findOrCreatePhoneForDevice(
                        model = buildInfoRepository.getModel(),
                        brandName = buildInfoRepository.getBrand(),
                    )
                }

                val createdIds = mutableListOf<String>()
                for (lens in ready.lenses) {
                    val created = withContext(Dispatchers.IO) {
                        submitRepository.createCamera(phone.id, lens)
                    }
                    createdIds += created.id
                }

                ready.photoUris.forEach { (index, uri) ->
                    val cameraId = createdIds.getOrNull(index) ?: return@forEach
                    withContext(Dispatchers.IO) {
                        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            ?: throw FileNotFoundException("Couldn't open $uri")
                        val exif = photoExifReader.read(contentResolver, uri)
                        if (!exifMatchesDevice(
                                exifMake = exif.make,
                                exifModel = exif.model,
                                manufacturer = buildInfoRepository.getBrand(),
                                brand = Build.BRAND,
                                model = buildInfoRepository.getModel(),
                            )
                        ) {
                            throw IllegalStateException("EXIF no longer matches this device")
                        }
                        submitRepository.uploadLensPhoto(cameraId, bytes, mimeType, exif)
                    }
                }

                _submitState.value = UploadSubmitState.Succeeded
            } catch (e: Exception) {
                Log.w(TAG, "Failed to upload device cameras", e)
                _submitState.value = UploadSubmitState.Failed(R.string.upload_error_submit)
            }
        }
    }

    fun submitMessageShown() {
        if (_submitState.value is UploadSubmitState.Failed) {
            _submitState.value = UploadSubmitState.Idle
        }
    }
}
