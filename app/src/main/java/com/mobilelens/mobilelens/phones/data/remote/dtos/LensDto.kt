package com.mobilelens.mobilelens.phones.data.remote.dtos

import kotlinx.serialization.Serializable

@Serializable
data class LensDto(
    val id: String,
    val smartphoneId: String,
    val status: String,
    val type: String,
    val facing: String,
    val focalLengthMm: Double,
    val aperture: Double,
    val cropFactor: Double,
    val pixelPitchUm: Double,
    val resolutionMp: Double,
    val activeResolutionMp: Double,
    val afZones: Int,
    val ois: String,
    val videoModes: List<VideoModeDto> = emptyList()
)

@Serializable
data class VideoModeDto(
    val id: String,
    val cameraId: String,
    val widthPx: Int,
    val heightPx: Int,
    val fpsMax: Double,
    val note: String?
)