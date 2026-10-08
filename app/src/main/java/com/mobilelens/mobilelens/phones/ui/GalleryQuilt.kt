package com.mobilelens.mobilelens.phones.ui

// Kept free of Compose so the row packing can be unit tested. A span wider than the row, or a
// row that doesn't fill it, crashes or clips the gallery grid.

internal const val GALLERY_QUILT_COLUMNS = 5

// Rows alternate between one full-width photo and a pair whose wider side switches every time:
// [full] [narrow | wide] [full] [wide | narrow] …
private val QuiltSpans = intArrayOf(GALLERY_QUILT_COLUMNS, 2, 3, GALLERY_QUILT_COLUMNS, 3, 2)

internal fun galleryQuiltSpan(index: Int, photoCount: Int): Int {
    val position = index % QuiltSpans.size
    val startsPair = position == 1 || position == 4
    // A pair with nothing to fill its second half becomes a full-width row
    return if (startsPair && index == photoCount - 1) GALLERY_QUILT_COLUMNS else QuiltSpans[position]
}
