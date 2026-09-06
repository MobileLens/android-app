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
    val imageUrl: String,
    val releaseDate: String,
    val viewCount: Int,
    val brandId: String,
    val brandName: String,
    val createdAt: String
)