package com.mobilelens.mobilelens.reviews.data.remote.dtos

import kotlinx.serialization.Serializable

@Serializable
data class ReviewCommentDto(
    val id: String,
    val reviewId: String,
    val authorId: String,
    val authorName: String? = null,
    val content: String,
    val createdAt: String,
)

@Serializable
data class CommentsResponse(
    val data: List<ReviewCommentDto>,
    val page: Int = 1,
    val limit: Int = 50,
)

@Serializable
data class CreateCommentRequest(
    val content: String,
)

@Serializable
data class LikeResponse(
    val liked: Boolean,
    val likeCount: Int,
)
