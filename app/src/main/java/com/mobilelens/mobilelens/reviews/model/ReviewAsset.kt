package com.mobilelens.mobilelens.reviews.model

enum class ReviewAssetType {
    IMAGE,
    VIDEO
}

data class ReviewAsset(
    val id: String,
    val type: ReviewAssetType,
    val storageUrl: String,
)