package com.mobilelens.mobilelens.phones.data

import com.mobilelens.mobilelens.phones.model.CatalogueSort
import com.mobilelens.mobilelens.phones.model.Facing
import com.mobilelens.mobilelens.phones.model.LensType
import com.mobilelens.mobilelens.phones.model.Stabilization
import org.junit.Test

import org.junit.Assert.*

/**
 * Verifies that the camera enum strings sent by the backend
 * (`camera` table in backend-api/api/src/db/schema.ts) map to the domain enums.
 */
class ApiEnumMappingTest {

    // --- LensType ---

    @Test
    fun lensType_mapsEveryBackendValue() {
        assertEquals(LensType.WIDE, lensTypeFromApi("wide"))
        assertEquals(LensType.ULTRAWIDE, lensTypeFromApi("ultrawide"))
        assertEquals(LensType.TELEPHOTO, lensTypeFromApi("tele"))
        assertEquals(LensType.MACRO, lensTypeFromApi("macro"))
        assertEquals(LensType.OTHER, lensTypeFromApi("other"))
    }

    @Test
    fun lensType_isCaseInsensitive() {
        assertEquals(LensType.WIDE, lensTypeFromApi("WIDE"))
        assertEquals(LensType.ULTRAWIDE, lensTypeFromApi("UltraWide"))
        assertEquals(LensType.TELEPHOTO, lensTypeFromApi("TELE"))
        assertEquals(LensType.MACRO, lensTypeFromApi("Macro"))
        assertEquals(LensType.OTHER, lensTypeFromApi("OTHER"))
    }

    @Test
    fun lensType_acceptsAliases() {
        assertEquals(LensType.ULTRAWIDE, lensTypeFromApi("ultra-wide"))
        assertEquals(LensType.TELEPHOTO, lensTypeFromApi("telephoto"))
        assertEquals(LensType.TELEPHOTO, lensTypeFromApi("Telephoto"))
    }

    @Test
    fun lensType_unknownValueFallsBackToOther() {
        assertEquals(LensType.OTHER, lensTypeFromApi("periscope"))
        assertEquals(LensType.OTHER, lensTypeFromApi(""))
    }

    // --- Facing ---

    @Test
    fun facing_mapsEveryBackendValue() {
        assertEquals(Facing.BACK, facingFromApi("back"))
        assertEquals(Facing.FRONT, facingFromApi("front"))
        assertEquals(Facing.OTHER, facingFromApi("other"))
    }

    @Test
    fun facing_isCaseInsensitive() {
        assertEquals(Facing.BACK, facingFromApi("BACK"))
        assertEquals(Facing.FRONT, facingFromApi("Front"))
        assertEquals(Facing.OTHER, facingFromApi("OTHER"))
    }

    @Test
    fun facing_acceptsRearAlias() {
        assertEquals(Facing.BACK, facingFromApi("rear"))
    }

    @Test
    fun facing_unknownValueFallsBackToOther() {
        assertEquals(Facing.OTHER, facingFromApi("external"))
        assertEquals(Facing.OTHER, facingFromApi(""))
    }

    // --- Stabilization ---

    @Test
    fun stabilization_mapsEveryBackendValue() {
        assertEquals(Stabilization.NONE, stabilizationFromApi("none"))
        assertEquals(Stabilization.OIS, stabilizationFromApi("optical"))
        assertEquals(Stabilization.SENSORSHIFT, stabilizationFromApi("sensor_shift"))
    }

    @Test
    fun stabilization_isCaseInsensitive() {
        assertEquals(Stabilization.NONE, stabilizationFromApi("NONE"))
        assertEquals(Stabilization.OIS, stabilizationFromApi("Optical"))
        assertEquals(Stabilization.SENSORSHIFT, stabilizationFromApi("SENSOR_SHIFT"))
    }

    @Test
    fun stabilization_acceptsAliases() {
        assertEquals(Stabilization.OIS, stabilizationFromApi("ois"))
        assertEquals(Stabilization.SENSORSHIFT, stabilizationFromApi("sensorshift"))
        assertEquals(Stabilization.SENSORSHIFT, stabilizationFromApi("sensor-shift"))
    }

    @Test
    fun stabilization_unknownValueFallsBackToNone() {
        assertEquals(Stabilization.NONE, stabilizationFromApi("gyro"))
        assertEquals(Stabilization.NONE, stabilizationFromApi(""))
    }

    // --- CatalogueSort ---

    @Test
    fun catalogueSort_apiValuesMatchBackendSorts() {
        // `SORTS` in backend-api/api/src/routes/smartphones.ts
        assertEquals(listOf("name", "new", "trending"), CatalogueSort.entries.map { it.apiValue }.sorted())
    }

    // --- Reverse mappings (catalogue filters) ---

    @Test
    fun lensType_toApiRoundTrips() {
        LensType.entries.forEach { assertEquals(it, lensTypeFromApi(lensTypeToApi(it))) }
    }

    @Test
    fun stabilization_toApiRoundTrips() {
        Stabilization.entries.forEach { assertEquals(it, stabilizationFromApi(stabilizationToApi(it))) }
    }
}
