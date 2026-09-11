package com.mobilelens.mobilelens.favorites.data.remote

import com.mobilelens.mobilelens.core.data.remote.dtos.OkResponse
import com.mobilelens.mobilelens.favorites.data.remote.dtos.FavoriteDto
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FavoriteApi {
    @GET("api/favorites")
    suspend fun getFavorites(): List<FavoriteDto>

    @POST("api/favorites/{smartphoneId}")
    suspend fun addFavorite(
        @Path("smartphoneId") smartphoneId: String
    ): OkResponse

    @DELETE("api/favorites/{smartphoneId}")
    suspend fun removeFavorite(
        @Path("smartphoneId") smartphoneId: String
    ): OkResponse
}
