package com.mobilelens.mobilelens.phones.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OpticsValuesTest {

    @Test
    fun opticsSpan_emptyIsNull() {
        assertNull(opticsSpan(emptyList()))
    }

    @Test
    fun opticsSpan_singleReading() {
        assertEquals(1.8f to 1.8f, opticsSpan(listOf(1.8f)))
    }

    @Test
    fun opticsSpan_rangeUsesMinAndMax() {
        // Camera2 aperture lists are ascending; focal lengths may not be when OEMs reorder them
        assertEquals(1.5f to 4.0f, opticsSpan(listOf(1.5f, 1.8f, 2.4f, 4.0f)))
        assertEquals(2.22f to 15.66f, opticsSpan(listOf(15.66f, 2.22f, 6.86f)))
    }

    @Test
    fun opticsSpan_duplicateStopsCollapseToOneReading() {
        assertEquals(1.8f to 1.8f, opticsSpan(listOf(1.8f, 1.8f)))
    }
}
