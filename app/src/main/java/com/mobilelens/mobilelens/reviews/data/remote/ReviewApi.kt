package com.mobilelens.mobilelens.reviews.data.remote

import com.mobilelens.mobilelens.core.data.remote.dtos.OkResponse
import com.mobilelens.mobilelens.reviews.data.remote.dtos.CreateReviewRequest
import com.mobilelens.mobilelens.reviews.data.remote.dtos.ReviewDto
import com.mobilelens.mobilelens.reviews.data.remote.dtos.UpdateReviewRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ReviewApi {
    @GET("api/reviews")
    suspend fun getReviews(
        @Query("smartphone_id") smartphoneId: String
    ): List<ReviewDto>

    @GET("api/reviews/pending")
    suspend fun getPendingReviews(): List<ReviewDto>

    @GET("api/reviews/{id}")
    suspend fun getReview(
        @Path("id") id: String
    ): ReviewDto

    @POST("api/reviews")
    suspend fun createReview(
        @Body body: CreateReviewRequest
    ): ReviewDto

    @PATCH("api/reviews/{id}")
    suspend fun updateReview(
        @Path("id") id: String,
        @Body body: UpdateReviewRequest
    ): OkResponse

    @DELETE("api/reviews/{id}")
    suspend fun deleteReview(
        @Path("id") id: String
    ): OkResponse
}
