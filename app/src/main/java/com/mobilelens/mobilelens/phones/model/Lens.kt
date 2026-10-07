package com.mobilelens.mobilelens.phones.model

// User-facing labels and formatted values live in phones/ui/LensLabels.kt (string resources)

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

enum class LensType {
    WIDE,
    ULTRAWIDE,
    TELEPHOTO,
    MACRO,
    OTHER,
}

/**
 * Represents a camera lens and its specifications.
 */
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
    val stabilization: Stabilization,
    val videoResolutions: List<VideoResolution>,
    val type: LensType,
) {
    val focalLength35mm = focalLength.map { it * cropFactor }
    val mainFocalLength35mm = focalLength35mm.firstOrNull() ?: 0f
    val aperture35mm = aperture.map { it * cropFactor }
    val mainAperture = aperture.firstOrNull() ?: 0f
}
