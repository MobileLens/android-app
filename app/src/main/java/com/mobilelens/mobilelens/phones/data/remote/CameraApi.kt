package com.mobilelens.mobilelens.phones.data.remote

import com.mobilelens.mobilelens.phones.data.remote.dtos.CreateCameraRequest
import com.mobilelens.mobilelens.phones.data.remote.dtos.LensDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CameraApi {
    @GET("api/cameras")
    suspend fun getCameras(
        @Query("smartphone_id") smartphoneId: String
    ): List<LensDto>

    @POST("api/cameras")
    suspend fun createCamera(
        @Body body: CreateCameraRequest
    ): LensDto
}
