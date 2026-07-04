package com.mobilelens.mobilelens.model

data class ReviewThread(
    val id: Int,
    val phone: Phone,
    val reviews: List<Review>?,
)