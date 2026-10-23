package com.mobilelens.mobilelens.phones.data

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

class JpegMetadataTest {
    private val image = BufferedImage(64, 48, BufferedImage.TYPE_INT_RGB).apply {
        for (x in 0 until width) for (y in 0 until height) {
            setRGB(x, y, ((x * 4) shl 16) or ((y * 5) shl 8) or 0x40)
        }
    }

    // Real JPEGs from the JDK's encoder: SOI, JFIF, tables, one scan or several, EOI
    private val baselineJpeg = encode(progressive = false)
    private val progressiveJpeg = encode(progressive = true)

    @Test
    fun removesEveryKindOfMetadataWithoutTouchingTheImage() {
        val stripped = stripJpegMetadata(withPhoneMetadata(baselineJpeg))

        assertArrayEquals(baselineJpeg, stripped)
    }

    @Test
    fun keepsEveryScanOfAProgressiveJpeg() {
        val stripped = stripJpegMetadata(withPhoneMetadata(progressiveJpeg))

        assertArrayEquals(progressiveJpeg, stripped)
        assertSamePixels(progressiveJpeg, stripped!!)
    }

    @Test
    fun keepsOrientationAsTheOnlyExifTag() {
        val stripped = stripJpegMetadata(withPhoneMetadata(baselineJpeg), orientation = 6)!!

        assertFalse(String(stripped, Charsets.ISO_8859_1).contains("secret"))
        // Right after JFIF: APP1, 34 bytes long, a big-endian TIFF with one Orientation entry set to 6
        val exifStart = jfifEnd(stripped)
        val expected = byteArrayOf(
            0xFF.toByte(), 0xE1.toByte(), 0, 34, 'E'.code.toByte(), 'x'.code.toByte(), 'i'.code.toByte(),
            'f'.code.toByte(), 0, 0, 'M'.code.toByte(), 'M'.code.toByte(), 0, 0x2A, 0, 0, 0, 8,
            0, 1, 0x01, 0x12, 0, 3, 0, 0, 0, 1, 0, 6, 0, 0, 0, 0, 0, 0,
        )
        assertArrayEquals(expected, stripped.copyOfRange(exifStart, exifStart + expected.size))
        assertSamePixels(baselineJpeg, stripped)
    }

    @Test
    fun leavesOutOrientationWhenThePhotoIsAlreadyUpright() {
        assertArrayEquals(baselineJpeg, stripJpegMetadata(baselineJpeg, orientation = 1))
        assertArrayEquals(baselineJpeg, stripJpegMetadata(baselineJpeg, orientation = null))
    }

    @Test
    fun keepsTheColourProfile() {
        val icc = segment(0xE2, "ICC_PROFILE".ascii() + byteArrayOf(0, 1, 1) + "profile".ascii())
        val stripped = stripJpegMetadata(insertAfterJfif(baselineJpeg, icc))!!

        assertTrue(String(stripped, Charsets.ISO_8859_1).contains("ICC_PROFILE"))
        assertEquals(baselineJpeg.size + icc.size, stripped.size)
    }

    @Test
    fun keepsStuffedBytesAndRestartMarkersInsideAScan() {
        val scanData = byteArrayOf(0x12, 0xFF.toByte(), 0x00, 0x34, 0xFF.toByte(), 0xD0.toByte(), 0x56)
        val jpeg = byteArrayOf(0xFF.toByte(), 0xD8.toByte()) +
            segment(0xDA, byteArrayOf(1, 1, 0, 0, 63, 0)) +
            scanData +
            byteArrayOf(0xFF.toByte(), 0xD9.toByte()) +
            "secret-trailer".ascii()

        assertArrayEquals(jpeg.copyOf(jpeg.size - "secret-trailer".length), stripJpegMetadata(jpeg))
    }

    @Test
    fun refusesWhatIsntACompleteJpeg() {
        val png = ByteArrayOutputStream().also { ImageIO.write(image, "png", it) }.toByteArray()
        assertNull(stripJpegMetadata(png))
        assertNull(stripJpegMetadata(ByteArray(0)))
        // Cut short: no end of image
        assertNull(stripJpegMetadata(baselineJpeg.copyOf(baselineJpeg.size - 10)))
        // A segment longer than the file
        assertNull(stripJpegMetadata(baselineJpeg.copyOf(30)))
    }

    private fun encode(progressive: Boolean): ByteArray {
        val out = ByteArrayOutputStream()
        val writer = ImageIO.getImageWritersByFormatName("jpg").next()
        ImageIO.createImageOutputStream(out).use { stream ->
            writer.output = stream
            val param = writer.defaultWriteParam
            if (progressive) param.progressiveMode = ImageWriteParam.MODE_DEFAULT
            writer.write(null, IIOImage(image, null, null), param)
        }
        writer.dispose()
        return out.toByteArray()
    }

    // What a phone's camera app adds around the image: EXIF with location, XMP, an MPF index,
    // IPTC, a comment, and a gain map and an OEM trailer after the end of the image
    private fun withPhoneMetadata(jpeg: ByteArray): ByteArray {
        val metadata = segment(0xE1, "Exif".ascii() + byteArrayOf(0, 0) + "MM GPS secret-location".ascii()) +
            segment(0xE1, "http://ns.adobe.com/xap/1.0/".ascii() + byteArrayOf(0) + "secret-xmp".ascii()) +
            segment(0xE2, "MPF".ascii() + byteArrayOf(0) + "secret-mpf".ascii()) +
            segment(0xED, "Photoshop 3.0".ascii() + byteArrayOf(0) + "secret-iptc".ascii()) +
            segment(0xFE, "secret-comment".ascii())
        val gainMap = byteArrayOf(0xFF.toByte(), 0xD8.toByte()) + "secret-gain-map".ascii() +
            byteArrayOf(0xFF.toByte(), 0xD9.toByte())
        return insertAfterJfif(jpeg, metadata) + gainMap + "secret-trailer".ascii()
    }

    private fun insertAfterJfif(jpeg: ByteArray, inserted: ByteArray): ByteArray {
        val at = jfifEnd(jpeg)
        return jpeg.copyOfRange(0, at) + inserted + jpeg.copyOfRange(at, jpeg.size)
    }

    // Where the JFIF segment that follows SOI ends
    private fun jfifEnd(jpeg: ByteArray): Int {
        check(jpeg[2] == 0xFF.toByte() && jpeg[3] == 0xE0.toByte()) { "Expected JFIF after SOI" }
        val length = ((jpeg[4].toInt() and 0xFF) shl 8) or (jpeg[5].toInt() and 0xFF)
        return 4 + length
    }

    private fun segment(marker: Int, payload: ByteArray): ByteArray {
        val length = payload.size + 2
        return byteArrayOf(0xFF.toByte(), marker.toByte(), (length shr 8).toByte(), length.toByte()) + payload
    }

    private fun String.ascii() = toByteArray(Charsets.US_ASCII)

    private fun assertSamePixels(expected: ByteArray, actual: ByteArray) {
        val expectedImage = ImageIO.read(ByteArrayInputStream(expected))
        val actualImage = ImageIO.read(ByteArrayInputStream(actual))
        assertNotNull("The stripped JPEG doesn't decode", actualImage)
        assertEquals(expectedImage.width, actualImage.width)
        assertEquals(expectedImage.height, actualImage.height)
        val expectedPixels = expectedImage.getRGB(0, 0, expectedImage.width, expectedImage.height, null, 0, expectedImage.width)
        val actualPixels = actualImage.getRGB(0, 0, actualImage.width, actualImage.height, null, 0, actualImage.width)
        assertArrayEquals(expectedPixels, actualPixels)
    }
}
