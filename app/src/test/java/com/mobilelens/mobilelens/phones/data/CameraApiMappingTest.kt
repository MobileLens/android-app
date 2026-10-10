package com.mobilelens.mobilelens.phones.data

import com.mobilelens.mobilelens.phones.model.Facing
import com.mobilelens.mobilelens.phones.model.Lens
import com.mobilelens.mobilelens.phones.model.LensType
import com.mobilelens.mobilelens.phones.model.Stabilization
import com.mobilelens.mobilelens.phones.model.VideoResolution
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * `POST api/cameras` validates enums strictly (api/src/lib/cameraSpec.ts), so what the app sends has to
 * be exactly the backend's values, not the aliases the FromApi helpers also accept.
 */
class CameraApiMappingTest {
    private val backendLensTypes = setOf("wide", "ultrawide", "tele", "macro", "other")
    private val backendFacings = setOf("back", "front", "other")
    private val backendOis = setOf("none", "optical", "sensor_shift")

    @Test
    fun lensType_sendsOnlyBackendValues() {
        LensType.entries.forEach { assert(it.toApi() in backendLensTypes) { "${it.name} -> ${it.toApi()}" } }
        assertEquals("tele", LensType.TELEPHOTO.toApi())
    }

    @Test
    fun facing_sendsOnlyBackendValues() {
        Facing.entries.forEach { assert(it.toApi() in backendFacings) { "${it.name} -> ${it.toApi()}" } }
    }

    @Test
    fun stabilization_sendsOnlyBackendValues() {
        Stabilization.entries.forEach { assert(it.toApi() in backendOis) { "${it.name} -> ${it.toApi()}" } }
        assertEquals("optical", Stabilization.OIS.toApi())
    }

    @Test
    fun sentValues_mapBackToTheSameDomainValue() {
        LensType.entries.forEach { assertEquals(it, lensTypeFromApi(it.toApi())) }
        Facing.entries.forEach { assertEquals(it, facingFromApi(it.toApi())) }
        Stabilization.entries.forEach { assertEquals(it, stabilizationFromApi(it.toApi())) }
    }

    @Test
    fun request_capsVideoModesAtBackendLimit() {
        val modes = List(40) { VideoResolution(width = 1920 + it, height = 1080, fps = 30) }
        val lens = Lens(
            focalLength = listOf(5.2f),
            aperture = listOf(1.8f),
            cropFactor = 6f,
            sensorTypeDenominator = 2.7f,
            facing = Facing.BACK,
            pixelPitchUm = 1.0f,
            resolution = 50f,
            activeResolution = 12.5f,
            afZones = 1,
            stabilization = Stabilization.OIS,
            videoResolutions = modes,
            type = LensType.TELEPHOTO,
        )

        val request = lens.toCreateCameraRequest("phone-1")

        assertEquals(30, request.videoModes.size)
        assertEquals("tele", request.type)
        assertEquals("optical", request.ois)
    }
}
