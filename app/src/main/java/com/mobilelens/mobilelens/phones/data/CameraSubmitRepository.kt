package com.mobilelens.mobilelens.phones.data

import com.mobilelens.mobilelens.core.data.remote.ApiClient
import com.mobilelens.mobilelens.core.data.remote.UploadApi
import com.mobilelens.mobilelens.core.data.remote.dtos.MediaUploadResponse
import com.mobilelens.mobilelens.phones.data.remote.CameraApi
import com.mobilelens.mobilelens.phones.data.remote.dtos.LensDto
import com.mobilelens.mobilelens.phones.model.Lens
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class CameraSubmitRepository(
    private val cameraApi: CameraApi = ApiClient.createService(),
    private val uploadApi: UploadApi = ApiClient.createService(),
) {
    suspend fun createCamera(smartphoneId: String, lens: Lens): LensDto =
        cameraApi.createCamera(lens.toCreateCameraRequest(smartphoneId))

    suspend fun uploadLensPhoto(
        cameraId: String,
        bytes: ByteArray,
        mimeType: String,
        exif: PhotoExif,
    ): MediaUploadResponse {
        val body = bytes.toRequestBody(mimeType.toMediaType())
        val filePart = MultipartBody.Part.createFormData("file", "photo.jpg", body)
        return uploadApi.uploadPhoto(
            file = filePart,
            cameraId = cameraId.toPlainBody(),
            widthPx = exif.widthPx.toString().toPlainBody(),
            heightPx = exif.heightPx.toString().toPlainBody(),
            exifFocalLength = exif.focalLengthMm?.toString()?.toPlainBody(),
            exifAperture = exif.aperture?.toString()?.toPlainBody(),
            exifIso = exif.iso?.toString()?.toPlainBody(),
            exifShutterSpeed = exif.shutterSpeed?.toPlainBody(),
        )
    }

    private fun String.toPlainBody() =
        toRequestBody("text/plain".toMediaType())
}
