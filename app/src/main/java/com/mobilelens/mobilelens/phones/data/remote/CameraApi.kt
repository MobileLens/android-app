package com.mobilelens.mobilelens.phones.data.remote

import com.mobilelens.mobilelens.core.data.remote.dtos.OkResponse
import com.mobilelens.mobilelens.phones.data.remote.dtos.CreateCameraRequest
import com.mobilelens.mobilelens.phones.data.remote.dtos.LensDto
import com.mobilelens.mobilelens.phones.data.remote.dtos.ReviewCameraRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CameraApi {
    @GET("api/cameras")
    suspend fun getCameras(
        @Query("smartphone_id") smartphoneId: String
    ): List<LensDto>

    @GET("api/cameras/pending")
    suspend fun getPendingCameras(): List<LensDto>

    @POST("api/cameras")
    suspend fun createCamera(
        @Body body: CreateCameraRequest
    ): LensDto

    @PATCH("api/cameras/{id}/review")
    suspend fun reviewCamera(
        @Path("id") id: String,
        @Body body: ReviewCameraRequest
    ): OkResponse
}
