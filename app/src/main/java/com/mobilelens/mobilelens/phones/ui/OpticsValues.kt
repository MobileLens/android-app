package com.mobilelens.mobilelens.phones.ui

/**
 * Bounds used when a lens reports several focal lengths or apertures.
 *
 * Camera2 lists every available stop; for the spec cards we show a single value when they are
 * all the same, and otherwise the min–max span (e.g. variable aperture or continuous zoom).
 */
internal fun opticsSpan(values: List<Float>): Pair<Float, Float>? {
    if (values.isEmpty()) return null
    return values.min() to values.max()
}
