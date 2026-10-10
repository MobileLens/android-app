package com.mobilelens.mobilelens.phones.data.remote

import com.mobilelens.mobilelens.core.data.remote.dtos.OkResponse
import com.mobilelens.mobilelens.phones.data.remote.dtos.CreatePhoneRequest
import com.mobilelens.mobilelens.phones.data.remote.dtos.PhoneDto
import com.mobilelens.mobilelens.phones.data.remote.dtos.PhonesResponse
import com.mobilelens.mobilelens.phones.data.remote.dtos.PhotosResponse
import com.mobilelens.mobilelens.phones.data.remote.dtos.UpdatePhoneRequest
import com.mobilelens.mobilelens.phones.data.remote.dtos.ViewCountResponse
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

    @GET("api/smartphones/{id}/photos")
    suspend fun getPhotos(
        @Path("id") id: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
    ): PhotosResponse

    // Reading a phone no longer counts as a view; the client reports it once per opened screen
    @POST("api/smartphones/{id}/view")
    suspend fun countView(
        @Path("id") id: String
    ): ViewCountResponse

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
