package com.mobilelens.mobilelens.reviews.model

data class ReviewComment(
    val id: String,
    val authorId: String,
    val author: String,
    val content: String,
    val createdAt: String,
)
