package com.mobilelens.mobilelens.reviews.model

enum class ReviewAssetType {
    IMAGE,
    VIDEO
}

data class ReviewAsset(
    val id: Int,
    val type: ReviewAssetType,
    val storageUrl: String,
)