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
    // `storage://<bucket>/<objectKey>`, a reference rather than something an image loader can open
    val storageUrl: String,
    // Short-lived signed link, usable straight away for a preview while the file is still private
    val url: String? = null
)
