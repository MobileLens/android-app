package com.mobilelens.mobilelens.phones.data.remote

import com.mobilelens.mobilelens.core.data.remote.dtos.OkResponse
import com.mobilelens.mobilelens.phones.data.remote.dtos.CreatePhoneRequest
import com.mobilelens.mobilelens.phones.data.remote.dtos.PhoneDto
import com.mobilelens.mobilelens.phones.data.remote.dtos.PhonesResponse
import com.mobilelens.mobilelens.phones.data.remote.dtos.UpdatePhoneRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface PhoneApi {
    @GET("api/smartphones/{id}")
    suspend fun getPhone(
        @Path("id") id: String
    ): PhoneDto

    @GET("api/smartphones")
    suspend fun getPhones(
        @Query("q") query: String? = null,
        @Query("brand_id") brandId: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): PhonesResponse

    @GET("api/smartphones/compare")
    suspend fun comparePhones(
        @Query("ids") ids: String
    ): List<PhoneDto>

    @POST("api/smartphones")
    suspend fun createPhone(
        @Body body: CreatePhoneRequest
    ): PhoneDto

    @PATCH("api/smartphones/{id}")
    suspend fun updatePhone(
        @Path("id") id: String,
        @Body body: UpdatePhoneRequest
    ): OkResponse

    @DELETE("api/smartphones/{id}")
    suspend fun deletePhone(
        @Path("id") id: String
    ): OkResponse
}
