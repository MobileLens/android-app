package com.mobilelens.mobilelens.phones.data

import com.mobilelens.mobilelens.phones.model.Facing
import com.mobilelens.mobilelens.phones.model.Lens
import com.mobilelens.mobilelens.phones.model.LensType
import com.mobilelens.mobilelens.phones.model.Stabilization
import com.mobilelens.mobilelens.phones.data.remote.dtos.CreateCameraRequest
import com.mobilelens.mobilelens.phones.data.remote.dtos.VideoModeInput

/**
 * Backend enum strings for [CreateCameraRequest], inverse of the FromApi helpers.
 * The backend validates these strictly (`camera` enums in schema.ts), so aliases the FromApi
 * helpers accept must not be sent: `POST api/cameras` answers 400 INVALID_FIELD for them.
 */
internal fun LensType.toApi(): String = when (this) {
    LensType.WIDE -> "wide"
    LensType.ULTRAWIDE -> "ultrawide"
    LensType.TELEPHOTO -> "tele"
    LensType.MACRO -> "macro"
    LensType.OTHER -> "other"
}

internal fun Facing.toApi(): String = when (this) {
    Facing.FRONT -> "front"
    Facing.BACK -> "back"
    Facing.OTHER -> "other"
}

internal fun Stabilization.toApi(): String = when (this) {
    Stabilization.OIS -> "optical"
    Stabilization.SENSORSHIFT -> "sensor_shift"
    Stabilization.NONE -> "none"
}

// The backend rejects more video modes than this
private const val MAX_VIDEO_MODES = 30

internal fun Lens.toCreateCameraRequest(smartphoneId: String): CreateCameraRequest =
    CreateCameraRequest(
        smartphoneId = smartphoneId,
        type = type.toApi(),
        facing = facing.toApi(),
        focalLengthMm = (focalLength.firstOrNull() ?: 0f).toDouble(),
        aperture = (aperture.firstOrNull() ?: 0f).toDouble(),
        cropFactor = cropFactor.toDouble(),
        pixelPitchUm = pixelPitchUm.toDouble(),
        resolutionMp = resolution.toDouble(),
        activeResolutionMp = activeResolution.toDouble(),
        afZones = afZones,
        ois = stabilization.toApi(),
        videoModes = videoResolutions.take(MAX_VIDEO_MODES).map { mode ->
            VideoModeInput(
                widthPx = mode.width,
                heightPx = mode.height,
                fpsMax = mode.fps.toDouble(),
            )
        },
    )
