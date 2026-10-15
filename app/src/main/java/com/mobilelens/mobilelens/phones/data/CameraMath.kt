package com.mobilelens.mobilelens.phones.data

import com.mobilelens.mobilelens.phones.model.Facing
import com.mobilelens.mobilelens.phones.model.Lens
import com.mobilelens.mobilelens.phones.model.LensType
import com.mobilelens.mobilelens.phones.model.Stabilization
import com.mobilelens.mobilelens.phones.model.VideoResolution
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt

// Calculations behind CameraHardwareRepository and lenses read from photos, kept free of Android
// types so they can be unit tested

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
 * several camera IDs, sometimes as a cropped readout (e.g. Samsung's narrower selfie framing, or
 * the Pixel 8's "2x telephoto"). A crop shrinks the sensor size, but keeps the lens and usually
 * the pixel pitch. See [isUnbinnedCropOf] for when it doesn't.
 */
internal fun Lens.isSameCameraAs(other: Lens): Boolean =
    facing == other.facing &&
        stabilization == other.stabilization &&
        focalLength.approximatelyEqual(other.focalLength) &&
        aperture.approximatelyEqual(other.aperture) &&
        (approximatelyEqual(pixelPitchUm, other.pixelPitchUm) || isUnbinnedCropOf(other) || other.isUnbinnedCropOf(this))

/**
 * Whether this reading is the middle of [full]'s sensor at native resolution, while [full] reads
 * the whole sensor binned. The pitches then differ by the binning factor. E.g. the Pixel 8 reads its
 * 50 MP main sensor 2x2 binned (12.5 MP, 2.4 μm) and fakes a 2x telephoto from its central
 * 12.5 MP (1.2 μm) under another camera ID.
 */
private fun Lens.isUnbinnedCropOf(full: Lens): Boolean = binningOf(full) != null

/** How many pixels [full] bins along each side, when this reading is its unbinned crop. */
private fun Lens.binningOf(full: Lens): Int? {
    val binning = full.pixelPitchUm / pixelPitchUm
    val wholeBinning = binning.roundToInt()
    val isUnbinnedCrop = wholeBinning in 2..4 &&
        approximatelyEqual(binning, wholeBinning.toFloat()) &&
        cropFactor > full.cropFactor
    return wholeBinning.takeIf { isUnbinnedCrop }
}

/**
 * This full-sensor reading with its sensor's native pixel pitch and resolution, taken from [crop],
 * a duplicate of it. Sensors that report their full resolution (the *_MAXIMUM_RESOLUTION keys)
 * already have both, but others (e.g. the Pixel 8's main camera) only describe the binned readout.
 * Their unbinned crop shows the native pitch. Otherwise this reading is returned unchanged.
 */
internal fun Lens.withNativePixelsFrom(crop: Lens): Lens {
    val binning = crop.binningOf(this) ?: return this
    return copy(
        pixelPitchUm = crop.pixelPitchUm,
        // Inherits activeResolution's rounding, e.g. 50.0 MP instead of the exact 50.1 MP
        resolution = (activeResolution * binning * binning * 10).roundToInt() / 10f,
    )
}

/**
 * Whether the two lenses have a focal length and aperture in common, e.g. because a photo was
 * taken with a camera that's already listed. Facing isn't compared, since EXIF doesn't record it.
 */
internal fun Lens.sharesOpticsWith(other: Lens): Boolean =
    focalLength.any { a -> other.focalLength.any { b -> approximatelyEqual(a, b, EXIF_TOLERANCE) } } &&
        aperture.any { a -> other.aperture.any { b -> approximatelyEqual(a, b, EXIF_TOLERANCE) } }

// EXIF can round more coarsely than Camera2, e.g. 1.74 mm written as 1.7 mm
private const val EXIF_TOLERANCE = 0.05f

/**
 * The lens a stock-camera photo was taken with, for cameras Camera2 doesn't expose. Null when
 * EXIF lacks the focal length, its 35 mm equivalent or the aperture.
 *
 * The sensor is sized from the 35 mm equivalent, so it's the area the photo covers: digital zoom
 * or a non-native aspect ratio make it come out smaller. EXIF doesn't record the facing or
 * stabilization, so those are placeholders for the user to correct.
 */
internal fun lensFromPhotoExif(exif: PhotoExif): Lens? {
    val focalLength = exif.focalLengthMm?.toFloat()?.takeIf { it > 0f } ?: return null
    val focalLength35mm = exif.focalLength35mm?.toFloat() ?: return null
    val aperture = exif.aperture?.toFloat()?.takeIf { it > 0f } ?: return null
    // Some OEMs write the 35 mm equivalent into FocalLength too, which leaves no sensor size
    if (focalLength35mm <= focalLength) return null

    val sensorDiagonalMm = FULL_FRAME_DIAGONAL_MM * focalLength / focalLength35mm
    val pixelDiagonal = hypot(exif.widthPx.toFloat(), exif.heightPx.toFloat())
    val sensorWidthMm = sensorDiagonalMm * exif.widthPx / pixelDiagonal
    val sensorHeightMm = sensorDiagonalMm * exif.heightPx / pixelDiagonal
    val resolution = megapixels(exif.widthPx, exif.heightPx)

    return Lens(
        focalLength = listOf(focalLength),
        aperture = listOf(aperture),
        cropFactor = cropFactor(sensorWidthMm, sensorHeightMm),
        sensorTypeDenominator = sensorTypeDenominator(sensorWidthMm, sensorHeightMm),
        facing = Facing.BACK,
        pixelPitchUm = pixelPitchUm(sensorWidthMm, exif.widthPx),
        resolution = resolution,
        activeResolution = resolution,
        afZones = 1,
        stabilization = Stabilization.NONE,
        videoResolutions = emptyList(),
        type = lensTypeFor(focalLength35mm),
    )
}

private fun List<Float>.approximatelyEqual(other: List<Float>): Boolean =
    size == other.size && zip(other).all { (a, b) -> approximatelyEqual(a, b) }

// 2% absorbs rounding differences between IDs, while f/1.8 vs f/2.2 or 1.0 vs 1.12 μm stay distinct
private fun approximatelyEqual(a: Float, b: Float, tolerance: Float = 0.02f): Boolean =
    abs(a - b) <= tolerance * max(abs(a), abs(b))
