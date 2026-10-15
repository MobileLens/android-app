package com.mobilelens.mobilelens.phones.data

import com.mobilelens.mobilelens.phones.model.Facing
import com.mobilelens.mobilelens.phones.model.Lens
import com.mobilelens.mobilelens.phones.model.LensType
import com.mobilelens.mobilelens.phones.model.Stabilization
import com.mobilelens.mobilelens.phones.model.VideoResolution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraMathTest {

    @Test
    fun megapixels_roundsToOneDecimal() {
        assertEquals(12.0f, megapixels(4000, 3000), 0.0001f)
        assertEquals(48.8f, megapixels(8064, 6048), 0.0001f)
        assertEquals(199.8f, megapixels(16320, 12240), 0.0001f) // 199.76 MP
    }

    @Test
    fun cropFactor_isRelativeToFullFrame() {
        assertEquals(1.0f, cropFactor(36f, 24f), 0.001f)
        assertEquals(2.0f, cropFactor(18f, 12f), 0.001f)
    }

    @Test
    fun sensorTypeDenominator_uses16mmPerInch() {
        // 12.8 x 9.6 mm has a 16 mm diagonal, i.e. a 1" type sensor
        assertEquals(1.0f, sensorTypeDenominator(12.8f, 9.6f), 0.001f)
        assertEquals(2.0f, sensorTypeDenominator(6.4f, 4.8f), 0.001f)
    }

    @Test
    fun pixelPitchUm_dividesSensorWidthByPixelCount() {
        assertEquals(1.5f, pixelPitchUm(6.0f, 4000), 0.0001f)
    }

    @Test
    fun lensTypeFor_classifiesBy35mmEquivalent() {
        assertEquals(LensType.ULTRAWIDE, lensTypeFor(13f))
        assertEquals(LensType.WIDE, lensTypeFor(21f))
        assertEquals(LensType.WIDE, lensTypeFor(24f))
        assertEquals(LensType.WIDE, lensTypeFor(36f))
        assertEquals(LensType.TELEPHOTO, lensTypeFor(77f))
    }

    @Test
    fun roundToStandardFps_snapsNearbyValues() {
        assertEquals(30, roundToStandardFps(29))
        assertEquals(60, roundToStandardFps(59))
        assertEquals(120, roundToStandardFps(119))
        assertEquals(240, roundToStandardFps(239))
        assertEquals(24, roundToStandardFps(24))
    }

    @Test
    fun videoResolutionsFrom_keepsLargest16By9ModesAtMaxFps() {
        val modes = listOf(
            StreamMode(3840, 2160, 33_333_333), // 30 fps
            StreamMode(3840, 2160, 16_666_666), // 60 fps, same size
            StreamMode(1920, 1080, 8_333_333), // 120 fps
            StreamMode(1440, 1080, 8_333_333), // 4:3
            StreamMode(640, 360, 8_333_333), // too small
            StreamMode(1280, 720, 0), // no duration reported
        )

        assertEquals(
            listOf(VideoResolution(3840, 2160, 60), VideoResolution(1920, 1080, 120)),
            videoResolutionsFrom(modes)
        )
    }

    @Test
    fun isSameCameraAs_matchesIdenticalReadings() {
        assertTrue(lens().isSameCameraAs(lens()))
    }

    @Test
    fun isSameCameraAs_toleratesTinyDifferences() {
        assertTrue(lens(focalLength = 5.43f).isSameCameraAs(lens(focalLength = 5.40f)))
    }

    @Test
    fun isSameCameraAs_matchesCroppedReadoutOfSameSensor() {
        // Galaxy S20 FE front camera: ID 1 reads the full sensor, ID 3 a centre crop of it
        val full = lens(focalLength = 3.72f, aperture = 2.2f, resolution = 8f, cropFactor = 6.63f, pixelPitchUm = 1.6f)
        val cropped = lens(focalLength = 3.72f, aperture = 2.2f, resolution = 5.2f, cropFactor = 8.19f, pixelPitchUm = 1.6f)

        assertTrue(cropped.isSameCameraAs(full))
    }

    @Test
    fun isSameCameraAs_distinguishesDifferentCameras() {
        assertFalse(lens(aperture = 1.8f).isSameCameraAs(lens(aperture = 2.6f)))
        assertFalse(lens(focalLength = 4.3f).isSameCameraAs(lens(focalLength = 5.2f)))
        assertFalse(lens(resolution = 12f, pixelPitchUm = 1.4f).isSameCameraAs(lens(resolution = 48f, pixelPitchUm = 0.7f)))
        assertFalse(lens(facing = Facing.BACK).isSameCameraAs(lens(facing = Facing.FRONT)))
    }

    @Test
    fun lensFromPhotoExif_sizesSensorFrom35mmEquivalent() {
        // Galaxy S20 FE telephoto, which Camera2 hides: 3.264 x 2.448 mm, 1.0 μm pixels
        val lens = lensFromPhotoExif(exif(focalLengthMm = 7.12, focalLength35mm = 76, aperture = 2.4))!!

        assertEquals(listOf(7.12f), lens.focalLength)
        assertEquals(listOf(2.4f), lens.aperture)
        assertEquals(76f / 7.12f, lens.cropFactor, 0.001f)
        assertEquals(1.0f, lens.pixelPitchUm, 0.01f)
        assertEquals(8.0f, lens.resolution, 0.0001f)
        assertEquals(LensType.TELEPHOTO, lens.type)
    }

    @Test
    fun lensFromPhotoExif_needsFocalLengthsAndAperture() {
        assertNull(lensFromPhotoExif(exif(focalLength35mm = null)))
        assertNull(lensFromPhotoExif(exif(focalLengthMm = null)))
        assertNull(lensFromPhotoExif(exif(aperture = null)))
        // FocalLength holding the 35 mm equivalent too leaves no sensor size to work out
        assertNull(lensFromPhotoExif(exif(focalLengthMm = 26.0, focalLength35mm = 26)))
    }

    @Test
    fun sharesOpticsWith_toleratesExifRounding() {
        val ultrawide = lens(focalLength = 1.74f, aperture = 2.2f)

        assertTrue(lens(focalLength = 1.7f, aperture = 2.2f, facing = Facing.FRONT).sharesOpticsWith(ultrawide))
        assertFalse(lens(focalLength = 7.12f, aperture = 2.4f).sharesOpticsWith(ultrawide))
    }

    private fun exif(
        focalLengthMm: Double? = 7.12,
        focalLength35mm: Int? = 76,
        aperture: Double? = 2.4,
    ) = PhotoExif(
        make = "samsung",
        model = "SM-G780G",
        widthPx = 3264,
        heightPx = 2448,
        focalLengthMm = focalLengthMm,
        focalLength35mm = focalLength35mm,
        aperture = aperture,
        iso = 50,
        shutterSpeed = "1/100",
    )

    private fun lens(
        focalLength: Float = 5.4f,
        aperture: Float = 1.8f,
        resolution: Float = 12f,
        cropFactor: Float = 4.5f,
        pixelPitchUm: Float = 1.4f,
        facing: Facing = Facing.BACK,
    ) = Lens(
        focalLength = listOf(focalLength),
        aperture = listOf(aperture),
        cropFactor = cropFactor,
        sensorTypeDenominator = 1.7f,
        facing = facing,
        pixelPitchUm = pixelPitchUm,
        resolution = resolution,
        activeResolution = resolution,
        afZones = 1,
        stabilization = Stabilization.OIS,
        videoResolutions = emptyList(),
        type = LensType.WIDE,
    )
}
