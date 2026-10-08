package com.mobilelens.mobilelens.phones.data

/**
 * EXIF fields used when uploading a stock-camera photo for a lens.
 *
 * [widthPx]/[heightPx] are the encoded pixel size (full-res evidence). Make/Model must match
 * this device before the file is sent.
 */
data class PhotoExif(
    val make: String?,
    val model: String?,
    val widthPx: Int,
    val heightPx: Int,
    val focalLengthMm: Double?,
    val aperture: Double?,
    val iso: Int?,
    val shutterSpeed: String?,
)

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
