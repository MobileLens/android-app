package com.mobilelens.mobilelens.phones.data

import com.mobilelens.mobilelens.phones.model.Facing
import com.mobilelens.mobilelens.phones.model.Lens
import com.mobilelens.mobilelens.phones.model.LensType
import com.mobilelens.mobilelens.phones.model.Stabilization
import com.mobilelens.mobilelens.phones.model.VideoResolution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun isSameCameraAs_distinguishesDifferentCameras() {
        assertFalse(lens(aperture = 1.8f).isSameCameraAs(lens(aperture = 2.6f)))
        assertFalse(lens(focalLength = 4.3f).isSameCameraAs(lens(focalLength = 5.2f)))
        assertFalse(lens(resolution = 12f).isSameCameraAs(lens(resolution = 48f)))
        assertFalse(lens(facing = Facing.BACK).isSameCameraAs(lens(facing = Facing.FRONT)))
    }

    private fun lens(
        focalLength: Float = 5.4f,
        aperture: Float = 1.8f,
        resolution: Float = 12f,
        facing: Facing = Facing.BACK,
    ) = Lens(
        focalLength = listOf(focalLength),
        aperture = listOf(aperture),
        cropFactor = 4.5f,
        sensorTypeDenominator = 1.7f,
        facing = facing,
        pixelPitchUm = 1.4f,
        resolution = resolution,
        activeResolution = resolution,
        afZones = 1,
        stabilization = Stabilization.OIS,
        videoResolutions = emptyList(),
        type = LensType.WIDE,
    )
}
