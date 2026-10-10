package com.mobilelens.mobilelens.reviews.data.remote

import com.mobilelens.mobilelens.core.data.remote.dtos.OkResponse
import com.mobilelens.mobilelens.reviews.data.remote.dtos.CommentsResponse
import com.mobilelens.mobilelens.reviews.data.remote.dtos.CreateCommentRequest
import com.mobilelens.mobilelens.reviews.data.remote.dtos.CreateReviewRequest
import com.mobilelens.mobilelens.reviews.data.remote.dtos.LikeResponse
import com.mobilelens.mobilelens.reviews.data.remote.dtos.ReviewCommentDto
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

    // Every review the signed-in user wrote, whatever its status, most recently updated first
    @GET("api/reviews/mine")
    suspend fun getMyReviews(): List<ReviewDto>

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

    // Only published reviews have comments; the oldest come first
    @GET("api/reviews/{id}/comments")
    suspend fun getComments(
        @Path("id") id: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
    ): CommentsResponse

    @POST("api/reviews/{id}/comments")
    suspend fun createComment(
        @Path("id") id: String,
        @Body body: CreateCommentRequest
    ): ReviewCommentDto

    // Only the comment's author or an admin may delete it
    @DELETE("api/reviews/comments/{commentId}")
    suspend fun deleteComment(
        @Path("commentId") commentId: String
    ): OkResponse

    @POST("api/reviews/{id}/like")
    suspend fun likeReview(
        @Path("id") id: String
    ): LikeResponse

    @DELETE("api/reviews/{id}/like")
    suspend fun unlikeReview(
        @Path("id") id: String
    ): LikeResponse
}
