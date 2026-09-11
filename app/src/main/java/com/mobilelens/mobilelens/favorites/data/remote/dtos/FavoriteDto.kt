package com.mobilelens.mobilelens.favorites.data.remote.dtos

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteDto(
    val smartphoneId: String,
    val addedAt: String,
    val modelName: String? = null,
    val imageUrl: String? = null,
    val brandName: String? = null
)
