package com.mobilelens.mobilelens.phones.model

import java.util.Locale

enum class Facing(val displayName: String) {
    FRONT("Front"),
    BACK("Back"),
    OTHER("Other"),
}

enum class Stabilization(val displayName: String) {
    NONE("None"),
    OIS("OIS"),
    SENSORSHIFT("Sensor-shift"),
}

enum class LensType(val displayName: String) {
    WIDE("Wide"),
    ULTRAWIDE("Ultra-wide"),
    TELEPHOTO("Telephoto"),
    MACRO("Macro"),
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

    val focalLength35mmLabel: String
        get() = focalLength35mm.format("%.0f", " mm")

    val focalLengthLabel: String
        get() = focalLength.format("%.2f", " mm")

    val apertureLabel: String
        get() = aperture.format("f/%.1f", separator = "/")

    val aperture35mmLabel: String
        get() = aperture35mm.format("f/%.2f", separator = "/")

    val resolutionLabel: String
        get() = String.format(Locale.US, "%.0f MP", resolution)

    val activeResolutionLabel: String
        get() = String.format(Locale.US, "%.0f MP", activeResolution)

    val sensorSizeLabel: String
        get() = String.format(Locale.US, "1/%.2f\"", sensorTypeDenominator)

    val pixelPitchLabel: String
        get() = String.format(Locale.US, "%.2f μm", pixelPitchUm)

    val cropFactorLabel: String
        get() = String.format(Locale.US, "%.2fx", cropFactor)

    private fun List<Float>.format(
        pattern: String,
        unit: String = "",
        separator: String = ", "
    ): String = joinToString(separator) {
        String.format(Locale.US, pattern, it)
    } + unit
}
