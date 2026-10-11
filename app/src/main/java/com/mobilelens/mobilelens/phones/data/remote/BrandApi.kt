package com.mobilelens.mobilelens.phones.data.remote

import com.mobilelens.mobilelens.phones.data.remote.dtos.BrandDto
import retrofit2.http.GET
import retrofit2.http.Path

interface BrandApi {
    @GET("api/brands")
    suspend fun getBrands(): List<BrandDto>

    @GET("api/brands/{id}")
    suspend fun getBrand(
        @Path("id") id: String
    ): BrandDto
}
