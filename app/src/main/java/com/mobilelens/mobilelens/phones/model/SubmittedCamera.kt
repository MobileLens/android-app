package com.mobilelens.mobilelens.phones.model

/** A camera the signed-in user submitted, with where it stands in moderation. */
data class SubmittedCamera(
    val id: String,
    val phoneId: String,
    // Null when the phone couldn't be looked up
    val phoneName: String?,
    val type: LensType,
    val facing: Facing,
    val focalLengthMm: Double,
    val resolutionMp: Double,
    // As in the backend schema: pending | approved | rejected
    val status: String,
    val submittedAt: String?,
)
