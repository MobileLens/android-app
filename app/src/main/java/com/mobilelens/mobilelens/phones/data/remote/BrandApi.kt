package com.mobilelens.mobilelens.phones.data.remote

import com.mobilelens.mobilelens.core.data.remote.dtos.OkResponse
import com.mobilelens.mobilelens.phones.data.remote.dtos.BrandDto
import com.mobilelens.mobilelens.phones.data.remote.dtos.CreateBrandRequest
import com.mobilelens.mobilelens.phones.data.remote.dtos.UpdateBrandRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface BrandApi {
    @GET("api/brands")
    suspend fun getBrands(): List<BrandDto>

    @GET("api/brands/{id}")
    suspend fun getBrand(
        @Path("id") id: String
    ): BrandDto

    @POST("api/brands")
    suspend fun createBrand(
        @Body body: CreateBrandRequest
    ): BrandDto

    @PATCH("api/brands/{id}")
    suspend fun updateBrand(
        @Path("id") id: String,
        @Body body: UpdateBrandRequest
    ): OkResponse

    @DELETE("api/brands/{id}")
    suspend fun deleteBrand(
        @Path("id") id: String
    ): OkResponse
}
