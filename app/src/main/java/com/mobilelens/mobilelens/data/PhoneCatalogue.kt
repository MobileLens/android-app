package com.mobilelens.mobilelens.data

import com.mobilelens.mobilelens.model.DeviceInfo
import com.mobilelens.mobilelens.model.Facing
import com.mobilelens.mobilelens.model.Lens
import com.mobilelens.mobilelens.model.Phone
import com.mobilelens.mobilelens.model.Stabilization
import java.math.BigDecimal
import java.util.Calendar
import java.util.GregorianCalendar

val PhoneCatalogue = listOf(
    Phone(
        id = 1,
        deviceInfo = DeviceInfo(
            brand = "Apple",
            model = "iPhone 16 Pro",
            releaseDate = dateOf(2024, Calendar.SEPTEMBER, 20),
            imageURL = null,
        ),
        lenses = listOf(
            Lens(
                focalLength = listOf(6.8f),
                aperture = listOf(1.78f),
                cropFactor = 3.5f,
                sensorTypeDenominator = 0f,
                facing = Facing.BACK,
                pixelPitchUm = 1.22f,
                resolution = 48.0f,
                activeResolution = 24.0f,
                afZones = 100,
                ois = Stabilization.SENSORSHIFT,
                videoResolutions = emptyList(),
            ),
            Lens(
                focalLength = listOf(2.2f),
                aperture = listOf(2.2f),
                cropFactor = 5.9f,
                sensorTypeDenominator = 0f,
                facing = Facing.BACK,
                pixelPitchUm = 0.7f,
                resolution = 48.0f,
                activeResolution = 12.0f,
                afZones = 100,
                ois = Stabilization.NONE,
                videoResolutions = emptyList(),
            ),
        ),
    ),
    Phone(
        id = 2,
        deviceInfo = DeviceInfo(
            brand = "Google",
            model = "Pixel 9 Pro",
            releaseDate = dateOf(2024, Calendar.SEPTEMBER, 4),
            imageURL = null,
        ),
        lenses = listOf(
            Lens(
                focalLength = listOf(6.9f),
                aperture = listOf(1.68f),
                cropFactor = 3.6f,
                sensorTypeDenominator = 0f,
                facing = Facing.BACK,
                pixelPitchUm = 1.2f,
                resolution = 50.0f,
                activeResolution = 12.5f,
                afZones = 100,
                ois = Stabilization.OIS,
                videoResolutions = emptyList(),
            ),
            Lens(
                focalLength = listOf(18.0f),
                aperture = listOf(2.8f),
                cropFactor = 6.3f,
                sensorTypeDenominator = 0f,
                facing = Facing.BACK,
                pixelPitchUm = 0.7f,
                resolution = 48.0f,
                activeResolution = 12.0f,
                afZones = 100,
                ois = Stabilization.OIS,
                videoResolutions = emptyList(),
            ),
        ),
    ),
    Phone(
        id = 3,
        deviceInfo = DeviceInfo(
            brand = "Samsung",
            model = "Galaxy S25 Ultra",
            releaseDate = dateOf(2025, Calendar.FEBRUARY, 7),
            imageURL = null,
        ),
        lenses = listOf(
            Lens(
                focalLength = listOf(6.3f),
                aperture = listOf(1.7f),
                cropFactor = 3.8f,
                sensorTypeDenominator = 0f,
                facing = Facing.BACK,
                pixelPitchUm = 0.6f,
                resolution = 200.0f,
                activeResolution = 12.5f,
                afZones = 100,
                ois = Stabilization.OIS,
                videoResolutions = emptyList(),
            ),
            Lens(
                focalLength = listOf(2.7f),
                aperture = listOf(2.2f),
                cropFactor = 5.4f,
                sensorTypeDenominator = 0f,
                facing = Facing.FRONT,
                pixelPitchUm = 1.12f,
                resolution = 12.0f,
                activeResolution = 12.0f,
                afZones = 0,
                ois = Stabilization.NONE,
                videoResolutions = emptyList(),
            ),
        ),
    ),
)

val Phone.displayName: String
    get() = "${deviceInfo.brand} ${deviceInfo.model}"

val Phone.focalLengthSummary: String
    get() = lenses
        .flatMap(Lens::focalLength35mm)
        .distinct()
        .sorted()
        .joinToString(", ") { it.toCompactString() }

fun Float.toCompactString(): String =
    BigDecimal.valueOf(toDouble()).stripTrailingZeros().toPlainString()

fun List<Phone>.filterByQuery(query: String): List<Phone> {
    val tokens = query.trim().lowercase().split(Regex("\\s+")).filter(String::isNotEmpty)
    if (tokens.isEmpty()) return this

    return filter { phone ->
        val searchableText = "${phone.deviceInfo.brand} ${phone.deviceInfo.model}".lowercase()
        tokens.all(searchableText::contains)
    }
}

private fun dateOf(year: Int, month: Int, day: Int) =
    GregorianCalendar(year, month, day).time
