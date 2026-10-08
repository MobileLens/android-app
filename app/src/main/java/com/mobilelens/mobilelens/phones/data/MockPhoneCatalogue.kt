package com.mobilelens.mobilelens.phones.data

import com.mobilelens.mobilelens.phones.model.DeviceInfo
import com.mobilelens.mobilelens.phones.model.Facing
import com.mobilelens.mobilelens.phones.model.GalleryPhoto
import com.mobilelens.mobilelens.phones.model.Lens
import com.mobilelens.mobilelens.phones.model.LensType
import com.mobilelens.mobilelens.phones.model.Phone
import com.mobilelens.mobilelens.phones.model.Stabilization
import com.mobilelens.mobilelens.phones.model.VideoResolution

// Approximate specs, only meant to give previews realistic content
val PhoneCatalogue = listOf(
    Phone(
        id = "1",
        deviceInfo = DeviceInfo(
            brand = "Apple",
            model = "iPhone 17 Pro",
            releaseDate = "2025-09-19",
            imageURL = null
        ),
        lenses = listOf(
            // Main, ~24 mm equivalent
            Lens(
                focalLength = listOf(6.86f),
                aperture = listOf(1.78f),
                cropFactor = 3.5f,
                sensorTypeDenominator = 1.3f,
                facing = Facing.BACK,
                pixelPitchUm = 1.22f,
                resolution = 48f,
                activeResolution = 24f,
                afZones = 100,
                stabilization = Stabilization.SENSORSHIFT,
                videoResolutions = listOf(
                    VideoResolution(3840, 2160, 120),
                    VideoResolution(1920, 1080, 240),
                ),
                type = LensType.WIDE,
            ),
            // Ultra-wide, ~14 mm equivalent
            Lens(
                focalLength = listOf(2.22f),
                aperture = listOf(2.2f),
                cropFactor = 6.3f,
                sensorTypeDenominator = 2.33f,
                facing = Facing.BACK,
                pixelPitchUm = 0.7f,
                resolution = 48f,
                activeResolution = 12f,
                afZones = 1,
                stabilization = Stabilization.NONE,
                videoResolutions = listOf(
                    VideoResolution(3840, 2160, 60),
                    VideoResolution(1920, 1080, 60),
                ),
                type = LensType.ULTRAWIDE,
            ),
            // Telephoto, ~100 mm equivalent
            Lens(
                focalLength = listOf(15.66f),
                aperture = listOf(2.8f),
                cropFactor = 6.4f,
                sensorTypeDenominator = 2.37f,
                facing = Facing.BACK,
                pixelPitchUm = 0.7f,
                resolution = 48f,
                activeResolution = 12f,
                afZones = 1,
                stabilization = Stabilization.OIS,
                videoResolutions = listOf(
                    VideoResolution(3840, 2160, 60),
                    VideoResolution(1920, 1080, 60),
                ),
                type = LensType.TELEPHOTO,
            ),
            // Front, ~23 mm equivalent
            Lens(
                focalLength = listOf(2.69f),
                aperture = listOf(1.9f),
                cropFactor = 8.5f,
                sensorTypeDenominator = 3.15f,
                facing = Facing.FRONT,
                pixelPitchUm = 1.0f,
                resolution = 18f,
                activeResolution = 12f,
                afZones = 1,
                stabilization = Stabilization.NONE,
                videoResolutions = listOf(
                    VideoResolution(3840, 2160, 60),
                    VideoResolution(1920, 1080, 60),
                ),
                type = LensType.WIDE,
            ),
        )
    )
)

val Phone.displayName: String
    get() = "${deviceInfo.brand} ${deviceInfo.model}"

// Unreachable URLs, so previews show the placeholder each photo has until it loads
val MockGalleryPhotos = List(7) { index ->
    GalleryPhoto(id = "$index", imageUrl = "https://example.com/photos/$index.jpg")
}
