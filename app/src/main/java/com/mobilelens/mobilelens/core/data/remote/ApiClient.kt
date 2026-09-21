package com.mobilelens.mobilelens.core.data.remote

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

object ApiClient {
    private const val BASE_URL = "http://localhost:3000/"

    private val json = Json { ignoreUnknownKeys = true }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    inline fun <reified T> createService(): T {
        return retrofit.create(T::class.java)
    }
}
