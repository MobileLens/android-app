package com.mobilelens.mobilelens.phones.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GalleryQuiltTest {

    @Test
    fun galleryQuiltSpan_alternatesFullWidthAndSwitchingPairs() {
        assertEquals(listOf(5, 2, 3, 5, 3, 2, 5), spans(7))
    }

    @Test
    fun galleryQuiltSpan_widensAnUnpairedTile() {
        assertEquals(listOf(listOf(5), listOf(5)), packedRows(2))
        assertEquals(listOf(listOf(5), listOf(2, 3), listOf(5), listOf(5)), packedRows(5))
    }

    @Test
    fun galleryQuiltSpan_loadingPlaceholdersFillFourRows() {
        assertEquals(
            listOf(listOf(5), listOf(2, 3), listOf(5), listOf(3, 2)),
            packedRows(6),
        )
    }

    @Test
    fun galleryQuiltSpan_packsEveryCountIntoFullRows() {
        for (photoCount in 1..24) {
            val rows = packedRows(photoCount)
            rows.forEachIndexed { rowIndex, row ->
                assertEquals(
                    "photoCount=$photoCount row=$rowIndex spans=$row",
                    GALLERY_QUILT_COLUMNS,
                    row.sum(),
                )
            }
        }
    }

    private fun spans(photoCount: Int): List<Int> =
        (0 until photoCount).map { galleryQuiltSpan(it, photoCount) }

    /** Packs spans the way the grid does, and fails if one doesn't fit the current row. */
    private fun packedRows(photoCount: Int): List<List<Int>> {
        val rows = mutableListOf<List<Int>>()
        var current = mutableListOf<Int>()
        for (index in 0 until photoCount) {
            val span = galleryQuiltSpan(index, photoCount)
            assertTrue(
                "span $span at index $index is outside 1..$GALLERY_QUILT_COLUMNS",
                span in 1..GALLERY_QUILT_COLUMNS,
            )
            if (current.sum() + span > GALLERY_QUILT_COLUMNS) {
                rows += current.toList()
                current = mutableListOf()
            }
            current += span
            if (current.sum() == GALLERY_QUILT_COLUMNS) {
                rows += current.toList()
                current = mutableListOf()
            }
        }
        assertTrue("photoCount=$photoCount left a short row $current", current.isEmpty())
        return rows
    }
}
