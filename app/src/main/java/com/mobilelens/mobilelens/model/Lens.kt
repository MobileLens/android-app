package com.mobilelens.mobilelens.model

import java.util.Locale

enum class Facing {
    FRONT,
    BACK,
    OTHER,
}

enum class Stabilization {
    NONE,
    OIS,
    SENSORSHIFT,
}

data class VideoResolution(
    val width: Int,
    val height: Int,
    val fps: Int
) {
    override fun toString() : String {
        return "${width}x${height}@$fps"
    }
}

data class Lens(
    val focalLength: List<Float>,
    val aperture: List<Float>,
    val cropFactor: Float,
    val sensorTypeDenominator: Float,
    val facing: Facing,
    val pixelPitchUm: Float,
    val resolution: Float,
    val activeResolution: Float,
    val afZones: Int,
    val ois: Stabilization,
    val videoResolutions: List<VideoResolution>
) {
    val focalLength35mm = focalLength.map { it * cropFactor }
    val aperture35mm = aperture.map { it * cropFactor }

    val facingLabel: String
        get() = when (facing) {
            Facing.FRONT -> "Front"
            Facing.BACK -> "Back"
            Facing.OTHER -> "Other"
        }

    val focalLength35mmLabel: String
        get() = focalLength35mm.joinToString(separator = ", ") {
            String.format(Locale.US, "%.0f", it)
        } + " mm"

    val focalLengthLabel: String
        get() = focalLength.joinToString(separator = ", ") {
            String.format(Locale.US, "%.2f", it)
        } + " mm"

    val apertureLabel: String
        get() = aperture.joinToString(separator = "/") {
            String.format(Locale.US, "f/%.1f", it)
        }

    val aperture35mmLabel: String
        get() = aperture35mm.joinToString(separator = "/") {
            String.format(Locale.US, "f/%.1f", it)
        }
}
