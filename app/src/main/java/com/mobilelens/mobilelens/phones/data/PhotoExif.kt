package com.mobilelens.mobilelens.phones.data

import com.mobilelens.mobilelens.phones.model.GalleryCamera
import kotlin.math.abs

/**
 * EXIF fields used when uploading a stock-camera photo for a lens.
 *
 * [widthPx]/[heightPx] are the encoded pixel size (full-res evidence). Make/Model must match
 * this device, and the optics the camera the photo is for ([checkPhotoLens]), before the file is
 * sent. The file itself goes without its metadata ([stripJpegMetadata]) except [orientation].
 */
data class PhotoExif(
    val make: String?,
    val model: String?,
    val widthPx: Int,
    val heightPx: Int,
    val focalLengthMm: Double?,
    val focalLength35mm: Int?,
    val aperture: Double?,
    val iso: Int?,
    val shutterSpeed: String?,
    // EXIF Orientation, 1 to 8: how the pixels have to be turned to show the photo upright
    val orientation: Int? = null,
)

/** How a photo's EXIF compares with the camera it's being uploaded for. */
enum class PhotoLensMatch {
    MATCHES,
    /** EXIF lacks the focal length or the aperture, so the lens can't be told. */
    NO_LENS_DATA,
    /** The focal length, the aperture or the pixel count rules this camera out. */
    OTHER_LENS,
}

// EXIF rounds more coarsely than the catalogue, e.g. 6.9 mm for a 6.81 mm lens or f/1.9 for f/1.85
private const val PHOTO_OPTICS_TOLERANCE = 0.05

// A photo may have fewer pixels than the sensor (binned, cropped), but not noticeably more
private const val PHOTO_RESOLUTION_TOLERANCE = 0.05

/**
 * Whether a photo with this [exif] was taken with [camera]: its focal length and aperture have to
 * be the camera's, anywhere in its zoom or aperture range if it has one, and it can't have more
 * pixels than the sensor. Some OEMs write the 35 mm equivalent as the focal length too, which is
 * then compared with the camera's equivalent instead.
 */
internal fun checkPhotoLens(exif: PhotoExif, camera: GalleryCamera): PhotoLensMatch {
    val focalLength = exif.focalLengthMm?.takeIf { it > 0 } ?: return PhotoLensMatch.NO_LENS_DATA
    val aperture = exif.aperture?.takeIf { it > 0 } ?: return PhotoLensMatch.NO_LENS_DATA

    val shortest = camera.focalLengthMm
    val longest = camera.focalLengthMaxMm ?: shortest
    val writesEquivalent = exif.focalLength35mm?.let { abs(it - focalLength) < 0.5 } == true
    val focalLengthFits = if (writesEquivalent) {
        isWithin(focalLength, shortest * camera.cropFactor, longest * camera.cropFactor)
    } else {
        isWithin(focalLength, shortest, longest)
    }
    val apertureFits = isWithin(aperture, camera.aperture, camera.apertureNarrow ?: camera.aperture)
    val megapixels = exif.widthPx.toLong() * exif.heightPx / 1_000_000.0
    val resolutionFits = camera.resolutionMp <= 0 ||
        megapixels <= camera.resolutionMp * (1 + PHOTO_RESOLUTION_TOLERANCE)

    return if (focalLengthFits && apertureFits && resolutionFits) {
        PhotoLensMatch.MATCHES
    } else {
        PhotoLensMatch.OTHER_LENS
    }
}

private fun isWithin(value: Double, bound: Double, otherBound: Double): Boolean =
    value >= minOf(bound, otherBound) * (1 - PHOTO_OPTICS_TOLERANCE) &&
        value <= maxOf(bound, otherBound) * (1 + PHOTO_OPTICS_TOLERANCE)

/** Lowercase alphanumerics only, so "Pixel 8 Pro" and "pixel_8_pro" compare equal. */
internal fun normalizeDeviceToken(value: String): String =
    value.lowercase().filter { it.isLetterOrDigit() }

/**
 * Whether EXIF Make/Model look like they were written by this phone.
 *
 * Make may match [manufacturer] or [brand]. Model must match [model], allowing either side to
 * contain the other after normalisation (OEM marketing names vs Build.MODEL).
 */
internal fun exifMatchesDevice(
    exifMake: String?,
    exifModel: String?,
    manufacturer: String,
    brand: String,
    model: String,
): Boolean {
    if (exifMake.isNullOrBlank() || exifModel.isNullOrBlank()) return false

    val makeToken = normalizeDeviceToken(exifMake)
    val manufacturerToken = normalizeDeviceToken(manufacturer)
    val brandToken = normalizeDeviceToken(brand)
    val makeOk = makeToken.isNotEmpty() && (
        makeToken == manufacturerToken ||
            makeToken == brandToken ||
            manufacturerToken.contains(makeToken) ||
            brandToken.contains(makeToken) ||
            makeToken.contains(manufacturerToken) ||
            makeToken.contains(brandToken)
        )

    val modelToken = normalizeDeviceToken(exifModel)
    val deviceModelToken = normalizeDeviceToken(model)
    val modelOk = modelToken.isNotEmpty() && deviceModelToken.isNotEmpty() && (
        modelToken == deviceModelToken ||
            modelToken.contains(deviceModelToken) ||
            deviceModelToken.contains(modelToken)
        )

    return makeOk && modelOk
}
