package com.mobilelens.mobilelens.phones.model

/** A catalogue camera of a phone, which gallery photos taken with it are uploaded for. */
data class GalleryCamera(
    val id: String,
    val type: LensType,
    val facing: Facing,
    val focalLengthMm: Double,
    // An optical zoom's longest focal length; null for a fixed one
    val focalLengthMaxMm: Double? = null,
    val aperture: Double,
    // A variable aperture's narrowest stop; null for a fixed one
    val apertureNarrow: Double? = null,
    val cropFactor: Double,
    // The sensor's full resolution, which no photo taken with it can exceed
    val resolutionMp: Double,
    // As in the backend schema: pending | approved
    val status: String,
)
