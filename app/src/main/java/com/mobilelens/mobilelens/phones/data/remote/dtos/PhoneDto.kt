package com.mobilelens.mobilelens.phones.data.remote.dtos

import kotlinx.serialization.Serializable

@Serializable
data class PhoneDto(
    val id: String,
    val brandId: String,
    val addedBy: String,
    val verifiedBy: String? = null,
    val modelName: String,
    val imageUrl: String? = null,
    val releaseDate: String? = null,
    val viewCount: Int = 0,
    val createdAt: String,
    val cameras: List<LensDto> = emptyList()
)