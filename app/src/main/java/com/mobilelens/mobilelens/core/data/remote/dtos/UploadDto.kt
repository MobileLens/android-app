package com.mobilelens.mobilelens.core.data.remote.dtos

import kotlinx.serialization.Serializable

@Serializable
data class MediaUploadResponse(
    val id: String,
    val status: String,
    val objectKey: String
)

@Serializable
data class StorageUploadResponse(
    val objectKey: String,
    val storageUrl: String
)
