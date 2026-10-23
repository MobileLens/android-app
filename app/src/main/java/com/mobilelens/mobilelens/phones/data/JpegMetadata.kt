package com.mobilelens.mobilelens.phones.data

import java.io.ByteArrayOutputStream

/** The only kind of photo uploaded, since [stripJpegMetadata] can clean it without re-encoding. */
internal const val JPEG_MIME_TYPE = "image/jpeg"

// JPEG markers: each segment starts with 0xFF and one of these
private const val MARKER_SOI = 0xD8
private const val MARKER_EOI = 0xD9
private const val MARKER_SOS = 0xDA
private const val MARKER_APP0 = 0xE0
private const val MARKER_APP1 = 0xE1
private const val MARKER_APP2 = 0xE2
private const val MARKER_APP15 = 0xEF
private const val MARKER_COM = 0xFE
private val RESTART_MARKERS = 0xD0..0xD7
private const val MARKER_TEM = 0x01

private val ORIENTATIONS = 2..8

/**
 * The JPEG [jpeg] without its metadata, for uploading: EXIF (location, capture time, the camera's
 * serial number, the thumbnail...), XMP, IPTC, comments and OEM segments go, and so does anything
 * after the image, such as an Ultra HDR gain map or an OEM trailer. The pixels aren't re-encoded.
 *
 * Kept: the JFIF header and the colour profile, which say how to show the pixels, and the
 * [orientation] (EXIF 1 to 8), written as the only EXIF tag so the photo isn't shown on its side.
 * Null when [jpeg] isn't a JPEG or is cut short, so a photo is never sent with its metadata.
 */
internal fun stripJpegMetadata(jpeg: ByteArray, orientation: Int? = null): ByteArray? {
    if (jpeg.size < 4 || jpeg.u8(0) != 0xFF || jpeg.u8(1) != MARKER_SOI) return null

    val out = ByteArrayOutputStream(jpeg.size)
    out.writeMarker(MARKER_SOI)
    // Written before the first segment kept after JFIF's, which has to come first
    var orientationPending = orientation in ORIENTATIONS

    var pos = 2
    while (pos < jpeg.size) {
        if (jpeg.u8(pos) != 0xFF) return null
        // A marker may be padded with any number of 0xFF fill bytes
        while (pos < jpeg.size && jpeg.u8(pos) == 0xFF) pos++
        if (pos >= jpeg.size) return null
        val marker = jpeg.u8(pos++)

        when (marker) {
            // The end of the main image: whatever follows is dropped
            MARKER_EOI -> {
                out.writeMarker(MARKER_EOI)
                return out.toByteArray()
            }
            // Markers without a length; outside a scan they carry nothing
            in RESTART_MARKERS, MARKER_TEM -> out.writeMarker(marker)
            else -> {
                if (pos + 2 > jpeg.size) return null
                val length = (jpeg.u8(pos) shl 8) or jpeg.u8(pos + 1)
                if (length < 2 || pos + length > jpeg.size) return null
                val segmentEnd = pos + length

                if (isImageSegment(marker, jpeg, pos + 2, segmentEnd)) {
                    if (orientationPending && marker != MARKER_APP0) {
                        out.write(orientationExif(orientation!!))
                        orientationPending = false
                    }
                    out.writeMarker(marker)
                    out.write(jpeg, pos, length)
                }
                pos = segmentEnd

                // A scan's compressed data follows its header, up to the next real marker
                if (marker == MARKER_SOS) {
                    val scanEnd = scanEnd(jpeg, pos) ?: return null
                    out.write(jpeg, pos, scanEnd - pos)
                    pos = scanEnd
                }
            }
        }
    }
    // No end of image
    return null
}

/**
 * Whether a segment describes the image rather than where, when or with what it was taken:
 * application segments other than JFIF and the ICC colour profile are metadata, as are comments.
 */
private fun isImageSegment(marker: Int, jpeg: ByteArray, dataStart: Int, end: Int): Boolean =
    when (marker) {
        MARKER_APP0 -> jpeg.hasAsciiAt(dataStart, end, "JFIF\u0000")
        MARKER_APP2 -> jpeg.hasAsciiAt(dataStart, end, "ICC_PROFILE\u0000")
        in MARKER_APP1..MARKER_APP15, MARKER_COM -> false
        else -> true
    }

/**
 * Where the compressed data starting at [start] ends: at the first 0xFF that isn't a stuffed
 * 0xFF00, a restart marker or fill. Null when the file ends first.
 */
private fun scanEnd(jpeg: ByteArray, start: Int): Int? {
    var i = start
    while (i + 1 < jpeg.size) {
        if (jpeg.u8(i) != 0xFF) {
            i++
            continue
        }
        val next = jpeg.u8(i + 1)
        when {
            next == 0x00 || next in RESTART_MARKERS -> i += 2
            next == 0xFF -> i++
            else -> return i
        }
    }
    return null
}

/** An APP1 segment with EXIF that holds nothing but the Orientation tag. */
private fun orientationExif(orientation: Int): ByteArray = bytesOf(
    0xFF, MARKER_APP1, 0x00, 34, // marker, then the length of everything after it
    'E'.code, 'x'.code, 'i'.code, 'f'.code, 0x00, 0x00,
    'M'.code, 'M'.code, 0x00, 0x2A, 0x00, 0x00, 0x00, 0x08, // big-endian TIFF header, IFD0 at 8
    0x00, 0x01, // one entry
    0x01, 0x12, 0x00, 0x03, 0x00, 0x00, 0x00, 0x01, 0x00, orientation, 0x00, 0x00, // Orientation, a SHORT
    0x00, 0x00, 0x00, 0x00, // no further IFD
)

private fun bytesOf(vararg values: Int) = ByteArray(values.size) { values[it].toByte() }

private fun ByteArray.u8(index: Int): Int = this[index].toInt() and 0xFF

private fun ByteArray.hasAsciiAt(start: Int, end: Int, text: String): Boolean =
    end - start >= text.length && text.indices.all { this[start + it] == text[it].code.toByte() }

private fun ByteArrayOutputStream.writeMarker(marker: Int) {
    write(0xFF)
    write(marker)
}
