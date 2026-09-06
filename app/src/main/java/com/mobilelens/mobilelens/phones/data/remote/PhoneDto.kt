package com.mobilelens.mobilelens.phones.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class PhoneDto(
    val id: String,
    val brandId: String,
    val addedBy: String,
    val verifiedBy: String?,
    val modelName: String,
    val imageUrl: String,
    val releaseDate: String,
    val viewCount: Int,
    val createdAt: String,
    val cameras: List<LensDto> = emptyList()
)