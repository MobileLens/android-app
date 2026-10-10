package com.mobilelens.mobilelens.core.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaUrlTest {
    private val base = "https://example.org/"

    @Test
    fun mapsStorageReferenceToPublicMediaPath() {
        assertEquals(
            "https://example.org/media/review-media/abc.jpg",
            publicMediaUrl("storage://review-media/abc.jpg", base),
        )
    }

    @Test
    fun mapsLegacyMinioReference() {
        assertEquals(
            "https://example.org/media/photos/cam/abc.jpg",
            publicMediaUrl("minio://photos/cam/abc.jpg", base),
        )
    }

    @Test
    fun toleratesBaseWithoutTrailingSlash() {
        assertEquals(
            "https://example.org/media/device-images/x.png",
            publicMediaUrl("storage://device-images/x.png", "https://example.org"),
        )
    }

    @Test
    fun returnsNullForOtherUrls() {
        assertNull(publicMediaUrl("https://example.org/media/review-media/abc.jpg", base))
        assertNull(publicMediaUrl("", base))
    }
}
