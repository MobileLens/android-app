package com.mobilelens.mobilelens.reviews.data.remote.dtos

import kotlinx.serialization.Serializable

@Serializable
data class ReviewMediaDto(
    val id: String,
    val reviewId: String,
    val type: String,
    val storageUrl: String,
    val displayOrder: Int
)

@Serializable
data class ReviewDto(
    val id: String,
    val authorId: String,
    val smartphoneId: String,
    val title: String,
    val contentMarkdown: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val media: List<ReviewMediaDto> = emptyList()
)

@Serializable
data class ReviewMediaInput(
    val objectKey: String,
    val type: String,
    val displayOrder: Int
)

@Serializable
data class CreateReviewRequest(
    val smartphoneId: String,
    val title: String,
    val contentMarkdown: String,
    val mediaItems: List<ReviewMediaInput> = emptyList()
)

@Serializable
data class UpdateReviewRequest(
    val title: String? = null,
    val contentMarkdown: String? = null,
    val status: String? = null
)
