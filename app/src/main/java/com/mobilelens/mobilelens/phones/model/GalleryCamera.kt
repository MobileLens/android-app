package com.mobilelens.mobilelens.phones.model

/** A catalogue camera of a phone, which gallery photos taken with it are uploaded for. */
data class GalleryCamera(
    val id: String,
    val type: LensType,
    val facing: Facing,
    val focalLengthMm: Double,
    val aperture: Double,
    // As in the backend schema: pending | approved
    val status: String,
)
