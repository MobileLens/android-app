package com.mobilelens.mobilelens.core.data.remote

import com.mobilelens.mobilelens.core.data.remote.dtos.MediaUploadResponse
import com.mobilelens.mobilelens.core.data.remote.dtos.StorageUploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadApi {
    @Multipart
    @POST("api/upload/photo/upload")
    suspend fun uploadPhoto(
        @Part file: MultipartBody.Part,
        @Part("cameraId") cameraId: RequestBody,
        @Part("widthPx") widthPx: RequestBody,
        @Part("heightPx") heightPx: RequestBody,
        @Part("exifFocalLength") exifFocalLength: RequestBody? = null,
        @Part("exifAperture") exifAperture: RequestBody? = null,
        @Part("exifIso") exifIso: RequestBody? = null,
        @Part("exifShutterSpeed") exifShutterSpeed: RequestBody? = null
    ): MediaUploadResponse

    @Multipart
    @POST("api/upload/video/upload")
    suspend fun uploadVideo(
        @Part file: MultipartBody.Part,
        @Part("cameraId") cameraId: RequestBody,
        @Part("widthPx") widthPx: RequestBody,
        @Part("heightPx") heightPx: RequestBody,
        @Part("fps") fps: RequestBody
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
