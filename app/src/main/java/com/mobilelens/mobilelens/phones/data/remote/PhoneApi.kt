package com.mobilelens.mobilelens.phones.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface PhoneApi {
    @GET("api/smartphones/{id}")
    suspend fun getPhone(
        @Path("id") id: String
    ): PhoneDto
}