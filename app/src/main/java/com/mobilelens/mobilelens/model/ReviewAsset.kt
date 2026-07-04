package com.mobilelens.mobilelens.model

enum class ReviewAssetType {
    IMAGE,
    VIDEO
}

data class ReviewAsset(
    val id: Int,
    val type: ReviewAssetType,
    val storageUrl: String,
)