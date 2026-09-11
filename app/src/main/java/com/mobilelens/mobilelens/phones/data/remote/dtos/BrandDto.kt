package com.mobilelens.mobilelens.phones.data.remote.dtos

import kotlinx.serialization.Serializable

@Serializable
data class BrandDto(
    val id: String,
    val name: String,
    val logoUrl: String? = null
)

@Serializable
data class CreateBrandRequest(
    val name: String,
    val logoUrl: String? = null
)

@Serializable
data class UpdateBrandRequest(
    val name: String? = null,
    val logoUrl: String? = null
)
