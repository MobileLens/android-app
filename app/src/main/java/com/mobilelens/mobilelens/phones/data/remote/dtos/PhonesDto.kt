package com.mobilelens.mobilelens.phones.data.remote.dtos

import kotlinx.serialization.Serializable

@Serializable
data class PhonesResponse(
    val data: List<PhoneListDto>,
    val page: Int,
    val limit: Int
)

@Serializable
data class PhoneListDto(
    val id: String,
    val modelName: String,
    val imageUrl: String? = null,
    val releaseDate: String? = null,
    val viewCount: Int = 0,
    val brandId: String,
    val brandName: String? = null,
    val createdAt: String
)