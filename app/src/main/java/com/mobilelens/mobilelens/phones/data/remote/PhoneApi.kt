package com.mobilelens.mobilelens.phones.data.remote

import com.mobilelens.mobilelens.phones.data.remote.dtos.PhoneDto
import com.mobilelens.mobilelens.phones.data.remote.dtos.PhonesResponse
import retrofit2.http.GET
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
}