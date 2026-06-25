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
    val id: String,
    val brand: String,
    val model: String,
    val focalLength: List<Double>,
    val aperture: List<Double>,
    val cropFactor: Double,
    val facing: Facing,
    val pixelPitchUm: Double,
    val resolution: Double,
    val activeResolution: Double,
    val afZones: Int,
    val ois: Stabilization,
    val videoResolutions: List<VideoResolution>
) {
    val focalLength35mm = focalLength.map { it * cropFactor }
    val aperture35mm = aperture.map { it * cropFactor }
}
