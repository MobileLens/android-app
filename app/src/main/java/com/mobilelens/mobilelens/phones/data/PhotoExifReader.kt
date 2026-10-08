package com.mobilelens.mobilelens.phones.data

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.FileNotFoundException

class PhotoExifReader {
    fun read(contentResolver: ContentResolver, uri: Uri): PhotoExif {
        val exif = contentResolver.openInputStream(uri)?.use { ExifInterface(it) }
            ?: throw FileNotFoundException("Couldn't open $uri")

        val (width, height) = imageSize(contentResolver, uri, exif)
        return PhotoExif(
            make = exif.getAttribute(ExifInterface.TAG_MAKE)?.trim()?.takeIf { it.isNotEmpty() },
            model = exif.getAttribute(ExifInterface.TAG_MODEL)?.trim()?.takeIf { it.isNotEmpty() },
            widthPx = width,
            heightPx = height,
            focalLengthMm = exif.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, Double.NaN)
                .takeUnless { it.isNaN() },
            aperture = exif.getAttributeDouble(ExifInterface.TAG_F_NUMBER, Double.NaN)
                .takeUnless { it.isNaN() },
            iso = exif.getAttributeInt(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY, -1)
                .takeIf { it > 0 },
            shutterSpeed = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME),
        )
    }

    private fun imageSize(
        contentResolver: ContentResolver,
        uri: Uri,
        exif: ExifInterface,
    ): Pair<Int, Int> {
        val exifWidth = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0)
        val exifHeight = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0)
        if (exifWidth > 0 && exifHeight > 0) return exifWidth to exifHeight

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth > 0 && bounds.outHeight > 0) {
            return bounds.outWidth to bounds.outHeight
        }
        throw IllegalArgumentException("Couldn't read image size for $uri")
    }
}
