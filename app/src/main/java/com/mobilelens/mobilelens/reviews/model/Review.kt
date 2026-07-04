package com.mobilelens.mobilelens.reviews.model

data class Review(
    val id: Int,
    val title: String,
    val author: String,
    val content: String, // Markdown
    val createdAt: String,
    val updatedAt: String,
    val commentCount: Int,
    val likeCount: Int,
    val assets: List<ReviewAsset>?,
)