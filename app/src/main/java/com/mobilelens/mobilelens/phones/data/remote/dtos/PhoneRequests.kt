package com.mobilelens.mobilelens.phones.data.remote.dtos

import kotlinx.serialization.Serializable

// The backend matches brandName to a brand ignoring case, or creates the brand when there's none
@Serializable
data class CreatePhoneRequest(
    val brandName: String,
    val modelName: String,
    val imageUrl: String? = null,
    val releaseDate: String? = null
)
