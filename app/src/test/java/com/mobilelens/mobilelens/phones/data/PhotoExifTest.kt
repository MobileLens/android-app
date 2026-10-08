package com.mobilelens.mobilelens.phones.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoExifTest {

    @Test
    fun normalizeDeviceToken_stripsPunctuationAndCase() {
        assertEquals("pixel8pro", normalizeDeviceToken("Pixel 8 Pro"))
        assertEquals("sms918b", normalizeDeviceToken("SM-S918B"))
    }

    @Test
    fun exifMatchesDevice_acceptsExactMakeAndModel() {
        assertTrue(
            exifMatchesDevice(
                exifMake = "Google",
                exifModel = "Pixel 8 Pro",
                manufacturer = "Google",
                brand = "google",
                model = "Pixel 8 Pro",
            )
        )
    }

    @Test
    fun exifMatchesDevice_acceptsBrandWhenMakeDiffersSlightly() {
        assertTrue(
            exifMatchesDevice(
                exifMake = "samsung",
                exifModel = "SM-S918B",
                manufacturer = "Samsung",
                brand = "samsung",
                model = "SM-S918B",
            )
        )
    }

    @Test
    fun exifMatchesDevice_rejectsMissingTags() {
        assertFalse(
            exifMatchesDevice(
                exifMake = null,
                exifModel = "Pixel 8 Pro",
                manufacturer = "Google",
                brand = "google",
                model = "Pixel 8 Pro",
            )
        )
    }

    @Test
    fun exifMatchesDevice_rejectsOtherPhone() {
        assertFalse(
            exifMatchesDevice(
                exifMake = "Google",
                exifModel = "Pixel 7",
                manufacturer = "Google",
                brand = "google",
                model = "Pixel 8 Pro",
            )
        )
    }
}
