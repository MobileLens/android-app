package com.mobilelens.mobilelens.phones.data

import com.mobilelens.mobilelens.phones.model.Facing
import com.mobilelens.mobilelens.phones.model.GalleryCamera
import com.mobilelens.mobilelens.phones.model.LensType
import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoLensCheckTest {
    // Pixel 8 main camera: 6.81 mm f/1.68, 50 MP sensor
    private val mainCamera = camera()

    @Test
    fun acceptsAPhotoFromTheCameraDespiteExifRounding() {
        assertEquals(PhotoLensMatch.MATCHES, checkPhotoLens(exif(focalLengthMm = 6.9, aperture = 1.7), mainCamera))
    }

    @Test
    fun acceptsBinnedAndFullResolutionPhotos() {
        assertEquals(PhotoLensMatch.MATCHES, checkPhotoLens(exif(widthPx = 4080, heightPx = 3072), mainCamera))
        assertEquals(PhotoLensMatch.MATCHES, checkPhotoLens(exif(widthPx = 8160, heightPx = 6144), mainCamera))
    }

    @Test
    fun rejectsAPhotoFromAnotherLens() {
        // The telephoto's focal length, or the ultrawide's aperture
        assertEquals(PhotoLensMatch.OTHER_LENS, checkPhotoLens(exif(focalLengthMm = 18.0, aperture = 2.8), mainCamera))
        assertEquals(PhotoLensMatch.OTHER_LENS, checkPhotoLens(exif(aperture = 2.2), mainCamera))
    }

    @Test
    fun rejectsMorePixelsThanTheSensorHas() {
        val twelveMegapixels = camera(resolutionMp = 12.2)
        assertEquals(PhotoLensMatch.OTHER_LENS, checkPhotoLens(exif(widthPx = 8160, heightPx = 6144), twelveMegapixels))
    }

    @Test
    fun needsFocalLengthAndAperture() {
        assertEquals(PhotoLensMatch.NO_LENS_DATA, checkPhotoLens(exif(focalLengthMm = null), mainCamera))
        assertEquals(PhotoLensMatch.NO_LENS_DATA, checkPhotoLens(exif(aperture = null), mainCamera))
        assertEquals(PhotoLensMatch.NO_LENS_DATA, checkPhotoLens(exif(aperture = 0.0), mainCamera))
    }

    @Test
    fun acceptsAnyStopOfAZoomOrVariableAperture() {
        val zoom = camera(focalLengthMm = 6.0, focalLengthMaxMm = 18.0, aperture = 1.5, apertureNarrow = 2.4)

        assertEquals(PhotoLensMatch.MATCHES, checkPhotoLens(exif(focalLengthMm = 12.0, aperture = 2.4), zoom))
        assertEquals(
            PhotoLensMatch.OTHER_LENS,
            checkPhotoLens(exif(focalLengthMm = 25.0, focalLength35mm = 98, aperture = 2.0), zoom),
        )
        assertEquals(PhotoLensMatch.OTHER_LENS, checkPhotoLens(exif(focalLengthMm = 12.0, aperture = 4.0), zoom))
    }

    @Test
    fun comparesEquivalentsWhenExifWritesThe35mmFocalLength() {
        // 6.81 mm × 3.9 ≈ 26.6 mm
        assertEquals(
            PhotoLensMatch.MATCHES,
            checkPhotoLens(exif(focalLengthMm = 27.0, focalLength35mm = 27), mainCamera),
        )
        assertEquals(
            PhotoLensMatch.OTHER_LENS,
            checkPhotoLens(exif(focalLengthMm = 77.0, focalLength35mm = 77), mainCamera),
        )
    }

    private fun camera(
        focalLengthMm: Double = 6.81,
        focalLengthMaxMm: Double? = null,
        aperture: Double = 1.68,
        apertureNarrow: Double? = null,
        resolutionMp: Double = 50.0,
    ) = GalleryCamera(
        id = "main",
        type = LensType.WIDE,
        facing = Facing.BACK,
        focalLengthMm = focalLengthMm,
        focalLengthMaxMm = focalLengthMaxMm,
        aperture = aperture,
        apertureNarrow = apertureNarrow,
        cropFactor = 3.9,
        resolutionMp = resolutionMp,
        status = "approved",
    )

    private fun exif(
        focalLengthMm: Double? = 6.81,
        focalLength35mm: Int? = 25,
        aperture: Double? = 1.68,
        widthPx: Int = 4080,
        heightPx: Int = 3072,
    ) = PhotoExif(
        make = "Google",
        model = "Pixel 8",
        widthPx = widthPx,
        heightPx = heightPx,
        focalLengthMm = focalLengthMm,
        focalLength35mm = focalLength35mm,
        aperture = aperture,
        iso = 50,
        shutterSpeed = "0.01",
    )
}
