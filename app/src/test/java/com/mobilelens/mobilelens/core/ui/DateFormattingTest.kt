package com.mobilelens.mobilelens.core.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneOffset
import java.util.Locale

class DateFormattingTest {

    @Test
    fun formatsPlainDate() {
        assertEquals("Sep 19, 2025", formatLocalizedDate("2025-09-19", Locale.US, ZoneOffset.UTC))
    }

    @Test
    fun formatsTimestampInGivenZone() {
        assertEquals("Sep 19, 2025", formatLocalizedDate("2025-09-19T10:00:00.000Z", Locale.US, ZoneOffset.UTC))
        // Same instant is already the next day in UTC+14
        assertEquals("Sep 20, 2025", formatLocalizedDate("2025-09-19T12:00:00Z", Locale.US, ZoneOffset.ofHours(14)))
    }

    @Test
    fun followsLocale() {
        assertEquals(
            formatLocalizedDate("2025-09-19", Locale.GERMANY, ZoneOffset.UTC),
            "19.09.2025"
        )
    }

    @Test
    fun leavesUnparseableValuesUnchanged() {
        assertEquals("01-07-2026", formatLocalizedDate("01-07-2026", Locale.US, ZoneOffset.UTC))
        assertEquals("", formatLocalizedDate("", Locale.US, ZoneOffset.UTC))
    }
}
