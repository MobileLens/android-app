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

data class Lens(
    val id: String,
    val brand: String,
    val model: String,
    val focalLength: Double,
    val focalLength35mm: Double,
    val aperture: Double,
    val aperture35mm: Double,
    val cropFactor: Double,
    val facing: Facing,
    val pixelPitchUm: Double,
    val resolution: Double,
    val activeResolution: Double,
    val afZones: Int,
    val ois: Stabilization,
) {

}
