package com.mobilelens.mobilelens.data

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
}
