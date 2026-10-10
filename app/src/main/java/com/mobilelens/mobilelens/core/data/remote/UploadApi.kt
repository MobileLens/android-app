package com.mobilelens.mobilelens.core.data.remote

import com.mobilelens.mobilelens.core.data.remote.dtos.MediaUploadResponse
import com.mobilelens.mobilelens.core.data.remote.dtos.StorageUploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

// Retrofit sends the parts in parameter order, and the backend streams the body: the photo/video
// fields it requires (cameraId, widthPx, ...) must come BEFORE `file`, or it answers
// 400 FIELDS_AFTER_FILE. Keep `file` last.
interface UploadApi {
    @Multipart
    @POST("api/upload/photo/upload")
    suspend fun uploadPhoto(
        @Part("cameraId") cameraId: RequestBody,
        @Part("widthPx") widthPx: RequestBody,
        @Part("heightPx") heightPx: RequestBody,
        @Part("exifFocalLength") exifFocalLength: RequestBody? = null,
        @Part("exifAperture") exifAperture: RequestBody? = null,
        @Part("exifIso") exifIso: RequestBody? = null,
        @Part("exifShutterSpeed") exifShutterSpeed: RequestBody? = null,
        @Part file: MultipartBody.Part,
    ): MediaUploadResponse

    @Multipart
    @POST("api/upload/video/upload")
    suspend fun uploadVideo(
        @Part("cameraId") cameraId: RequestBody,
        @Part("widthPx") widthPx: RequestBody,
        @Part("heightPx") heightPx: RequestBody,
        @Part("fps") fps: RequestBody,
        @Part file: MultipartBody.Part,
    ): MediaUploadResponse

    @Multipart
    @POST("api/upload/review-media/upload")
    suspend fun uploadReviewMedia(
        @Part file: MultipartBody.Part
    ): StorageUploadResponse

    @Multipart
    @POST("api/upload/device-image/upload")
    suspend fun uploadDeviceImage(
        @Part file: MultipartBody.Part
    ): StorageUploadResponse
}
