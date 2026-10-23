package com.mobilelens.mobilelens.phones.data

import com.mobilelens.mobilelens.core.data.remote.ApiClient
import com.mobilelens.mobilelens.core.data.remote.UploadApi
import com.mobilelens.mobilelens.core.data.remote.dtos.MediaUploadResponse
import com.mobilelens.mobilelens.phones.data.remote.CameraApi
import com.mobilelens.mobilelens.phones.data.remote.dtos.LensDto
import com.mobilelens.mobilelens.phones.model.Facing
import com.mobilelens.mobilelens.phones.model.GalleryCamera
import com.mobilelens.mobilelens.phones.model.Lens
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

// Back cameras first, since they take most photos
private val GalleryFacingOrder = listOf(Facing.BACK, Facing.FRONT, Facing.OTHER)

class CameraSubmitRepository(
    private val cameraApi: CameraApi = ApiClient.createService(),
    private val uploadApi: UploadApi = ApiClient.createService(),
) {
    suspend fun createCamera(smartphoneId: String, lens: Lens): LensDto =
        cameraApi.createCamera(lens.toCreateCameraRequest(smartphoneId))

    /**
     * The cameras of [phoneId] that photos can be uploaded for: its catalogue cameras, plus the
     * ones the signed-in user submitted for it that are still in review.
     */
    suspend fun getGalleryCameras(phoneId: String): List<GalleryCamera> = coroutineScope {
        val listed = async { cameraApi.getCameras(phoneId) }
        val mine = async { cameraApi.getMyCameras() }
        galleryCamerasFrom(phoneId, listed = listed.await(), mine = mine.await())
    }

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

/**
 * The cameras photos of [phoneId] can be uploaded for: the [listed] ones and the user's own
 * ([mine], which spans every phone) for it, each once and without rejected ones. Back cameras come
 * first, each facing in [com.mobilelens.mobilelens.phones.model.LensType] order, then by focal length.
 */
internal fun galleryCamerasFrom(
    phoneId: String,
    listed: List<LensDto>,
    mine: List<LensDto>,
): List<GalleryCamera> =
    (listed + mine.filter { it.smartphoneId == phoneId })
        .filter { it.status != "rejected" }
        .distinctBy { it.id }
        .map { dto ->
            GalleryCamera(
                id = dto.id,
                type = lensTypeFromApi(dto.type),
                facing = facingFromApi(dto.facing),
                focalLengthMm = dto.focalLengthMm,
                aperture = dto.aperture,
                status = dto.status,
            )
        }
        .sortedWith(
            compareBy({ GalleryFacingOrder.indexOf(it.facing) }, { it.type.ordinal }, { it.focalLengthMm })
        )
