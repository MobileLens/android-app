package com.mobilelens.mobilelens.core.data.remote

import com.mobilelens.mobilelens.BuildConfig
import com.mobilelens.mobilelens.core.data.AuthTokenStore
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object ApiClient {
    val BASE_URL: String = BuildConfig.BASE_URL

    private var tokenStore: AuthTokenStore? = null

    // Backed by Keystore-encrypted on-disk store once [init] has run
    var authToken: String?
        get() = memoryToken ?: tokenStore?.token.also { memoryToken = it }
        set(value) {
            memoryToken = value
            tokenStore?.token = value
        }

    @Volatile
    private var memoryToken: String? = null

    fun init(store: AuthTokenStore) {
        tokenStore = store
        memoryToken = store.token
    }

    internal val json = Json { ignoreUnknownKeys = true }

    private val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                val currentToken = authToken
                if (!currentToken.isNullOrEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $currentToken")
                }
                chain.proceed(requestBuilder.build())
            }

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        builder.build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    inline fun <reified T> createService(): T {
        return retrofit.create(T::class.java)
    }
}
