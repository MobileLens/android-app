package com.mobilelens.mobilelens.reviews.model

import com.mobilelens.mobilelens.phones.model.Phone

data class ReviewThread(
    val id: Int,
    val phone: Phone,
    val reviews: List<Review>?,
)