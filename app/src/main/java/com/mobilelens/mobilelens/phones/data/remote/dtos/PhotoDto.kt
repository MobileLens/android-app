package com.mobilelens.mobilelens.phones.data.remote.dtos

import kotlinx.serialization.Serializable

@Serializable
data class PhotosResponse(
    val data: List<PhotoDto>,
    val page: Int = 1,
    val limit: Int = 20
)

/** A verified photo of a phone, from `GET api/smartphones/{id}/photos`. */
@Serializable
data class PhotoDto(
    val id: String,
    val cameraId: String,
    val cameraType: String? = null,
    val facing: String? = null,
    // Public HTTP URL, unlike the `storage://` reference the backend keeps in its database
    val url: String? = null,
    val widthPx: Int = 0,
    val heightPx: Int = 0,
    val uploadDate: String? = null
)

@Serializable
data class ViewCountResponse(
    val counted: Boolean
)
