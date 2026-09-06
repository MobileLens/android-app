package com.mobilelens.mobilelens.phones.data.remote.dtos

import kotlinx.serialization.Serializable

@Serializable
data class CreatePhoneRequest(
    val brandId: String,
    val modelName: String,
    val imageUrl: String? = null,
    val releaseDate: String? = null
)

@Serializable
data class UpdatePhoneRequest(
    val modelName: String? = null,
    val imageUrl: String? = null,
    val releaseDate: String? = null,
    val brandId: String? = null
)
