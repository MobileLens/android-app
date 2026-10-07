package com.mobilelens.mobilelens.phones.data

import com.mobilelens.mobilelens.phones.model.Lens
import com.mobilelens.mobilelens.phones.model.LensType
import com.mobilelens.mobilelens.phones.model.VideoResolution
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt

// Calculations behind CameraHardwareRepository, kept free of Android types so they can be unit tested

// Diagonal of a full-frame 36 x 24 mm sensor
private val FULL_FRAME_DIAGONAL_MM = hypot(36f, 24f)

/** Megapixels, rounded to one decimal place. */
internal fun megapixels(width: Int, height: Int): Float =
    (width.toLong() * height / 100_000f).roundToInt() / 10f

/** Crop factor = full-frame diagonal / sensor diagonal. */
internal fun cropFactor(sensorWidthMm: Float, sensorHeightMm: Float): Float =
    FULL_FRAME_DIAGONAL_MM / hypot(sensorWidthMm, sensorHeightMm)

/** Denominator x of the 1/x" sensor type, using the customary 16 mm per "inch". */
internal fun sensorTypeDenominator(sensorWidthMm: Float, sensorHeightMm: Float): Float =
    16f / hypot(sensorWidthMm, sensorHeightMm)

/** Pixel pitch in μm, assuming square pixels. */
internal fun pixelPitchUm(sensorWidthMm: Float, pixelArrayWidth: Int): Float =
    sensorWidthMm / pixelArrayWidth * 1000f

internal fun lensTypeFor(focalLength35mm: Float): LensType = when {
    focalLength35mm < 21f -> LensType.ULTRAWIDE
    focalLength35mm > 36f -> LensType.TELEPHOTO
    else -> LensType.WIDE
}

internal fun roundToStandardFps(fps: Int): Int = when (fps) {
    in 28..31 -> 30
    in 58..62 -> 60
    in 118..122 -> 120
    in 238..242 -> 240
    else -> fps
}

/** An output size with its minimum frame duration, as reported by StreamConfigurationMap. */
internal data class StreamMode(val width: Int, val height: Int, val minFrameDurationNs: Long)

/** 16:9 modes from 720p up, one per resolution at its highest frame rate, largest first. */
internal fun videoResolutionsFrom(modes: List<StreamMode>): List<VideoResolution> =
    modes
        .filter { it.minFrameDurationNs > 0 && isVideoSize(it.width, it.height) }
        .map {
            val maxFps = (1_000_000_000L / it.minFrameDurationNs).toInt()
            VideoResolution(it.width, it.height, roundToStandardFps(maxFps))
        }
        .groupBy { it.width to it.height }
        .map { (_, sameSize) -> sameSize.maxBy { it.fps } }
        .sortedByDescending { it.width * it.height }

private fun isVideoSize(width: Int, height: Int): Boolean {
    val is16By9 = abs(width.toFloat() / height - 16f / 9f) < 0.01f
    return is16By9 && width > 1200 && height > 700
}

/**
 * Whether two readings describe the same physical camera. Some OEMs expose one sensor under
 * several camera IDs.
 */
internal fun Lens.isSameCameraAs(other: Lens): Boolean =
    facing == other.facing &&
        stabilization == other.stabilization &&
        approximatelyEqual(resolution, other.resolution) &&
        approximatelyEqual(activeResolution, other.activeResolution) &&
        approximatelyEqual(cropFactor, other.cropFactor) &&
        focalLength.approximatelyEqual(other.focalLength) &&
        aperture.approximatelyEqual(other.aperture)

private fun List<Float>.approximatelyEqual(other: List<Float>): Boolean =
    size == other.size && zip(other).all { (a, b) -> approximatelyEqual(a, b) }

// 2% absorbs rounding differences between IDs, while f/1.8 vs f/2.2 or 12 vs 13 MP stay distinct
private fun approximatelyEqual(a: Float, b: Float, tolerance: Float = 0.02f): Boolean =
    abs(a - b) <= tolerance * max(abs(a), abs(b))
