package com.mobilelens.mobilelens.phones.data.remote.dtos

import kotlinx.serialization.Serializable

@Serializable
data class VideoModeInput(
    val widthPx: Int,
    val heightPx: Int,
    val fpsMax: Double,
    val note: String? = null
)

@Serializable
data class CreateCameraRequest(
    val smartphoneId: String,
    val type: String,
    val facing: String,
    val focalLengthMm: Double,
    val aperture: Double,
    val cropFactor: Double,
    val pixelPitchUm: Double,
    val resolutionMp: Double,
    val activeResolutionMp: Double,
    val afZones: Int = 0,
    val ois: String = "none",
    val videoModes: List<VideoModeInput> = emptyList()
)

@Serializable
data class ReviewCameraRequest(
    val status: String,
    val focalLengthMm: Double? = null,
    val aperture: Double? = null,
    val cropFactor: Double? = null,
    val pixelPitchUm: Double? = null,
    val resolutionMp: Double? = null,
    val activeResolutionMp: Double? = null,
    val afZones: Int? = null,
    val ois: String? = null
)
