package com.mobilelens.mobilelens.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Formats a date from the backend for the current app locale, e.g. "Sep 19, 2025" or "19 wrz 2025".
 * Values that aren't ISO-8601 dates or timestamps are shown unchanged.
 */
@Composable
fun localizedDate(value: String): String {
    val locale = LocalConfiguration.current.locales[0]
    return remember(value, locale) { formatLocalizedDate(value, locale) }
}

internal fun formatLocalizedDate(
    value: String,
    locale: Locale,
    zone: ZoneId = ZoneId.systemDefault(),
): String {
    val date = parseDate(value, zone) ?: return value
    return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).format(date)
}

// Accepts a plain date ("2025-09-19") or a timestamp with offset ("2025-09-19T10:00:00.000Z")
private fun parseDate(value: String, zone: ZoneId): LocalDate? =
    runCatching { LocalDate.parse(value) }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(value).atZoneSameInstant(zone).toLocalDate() }.getOrNull()
