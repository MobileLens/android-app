package com.mobilelens.mobilelens.data

import java.math.BigDecimal
import java.util.Calendar
import java.util.GregorianCalendar

val PhoneCatalogue = listOf(
    Phone(
        id = 1,
        brand = "Apple",
        model = "iPhone 16 Pro",
        releaseDate = dateOf(2024, Calendar.SEPTEMBER, 20),
        lenses = listOf(
            Lens(
                id = "iphone-16-pro-main",
                brand = "Apple",
                model = "Main Camera",
                focalLength = 6.8,
                focalLength35mm = 24.0,
                aperture = 1.78,
                aperture35mm = 6.3,
                cropFactor = 3.5,
                facing = Facing.BACK,
                pixelPitchUm = 1.22,
                resolution = 48.0,
                activeResolution = 24.0,
                afZones = 100,
                ois = Stabilization.SENSORSHIFT,
            ),
            Lens(
                id = "iphone-16-pro-ultrawide",
                brand = "Apple",
                model = "Ultra Wide Camera",
                focalLength = 2.2,
                focalLength35mm = 13.0,
                aperture = 2.2,
                aperture35mm = 13.0,
                cropFactor = 5.9,
                facing = Facing.BACK,
                pixelPitchUm = 0.7,
                resolution = 48.0,
                activeResolution = 12.0,
                afZones = 100,
                ois = Stabilization.NONE,
            ),
        ),
    ),
    Phone(
        id = 2,
        brand = "Google",
        model = "Pixel 9 Pro",
        releaseDate = dateOf(2024, Calendar.SEPTEMBER, 4),
        lenses = listOf(
            Lens(
                id = "pixel-9-pro-wide",
                brand = "Google",
                model = "Wide Camera",
                focalLength = 6.9,
                focalLength35mm = 25.0,
                aperture = 1.68,
                aperture35mm = 6.1,
                cropFactor = 3.6,
                facing = Facing.BACK,
                pixelPitchUm = 1.2,
                resolution = 50.0,
                activeResolution = 12.5,
                afZones = 100,
                ois = Stabilization.OIS,
            ),
            Lens(
                id = "pixel-9-pro-telephoto",
                brand = "Google",
                model = "5x Telephoto Camera",
                focalLength = 18.0,
                focalLength35mm = 113.0,
                aperture = 2.8,
                aperture35mm = 17.6,
                cropFactor = 6.3,
                facing = Facing.BACK,
                pixelPitchUm = 0.7,
                resolution = 48.0,
                activeResolution = 12.0,
                afZones = 100,
                ois = Stabilization.OIS,
            ),
        ),
    ),
    Phone(
        id = 3,
        brand = "Samsung",
        model = "Galaxy S25 Ultra",
        releaseDate = dateOf(2025, Calendar.FEBRUARY, 7),
        lenses = listOf(
            Lens(
                id = "galaxy-s25-ultra-wide",
                brand = "Samsung",
                model = "Wide Camera",
                focalLength = 6.3,
                focalLength35mm = 24.0,
                aperture = 1.7,
                aperture35mm = 6.0,
                cropFactor = 3.8,
                facing = Facing.BACK,
                pixelPitchUm = 0.6,
                resolution = 200.0,
                activeResolution = 12.5,
                afZones = 100,
                ois = Stabilization.OIS,
            ),
            Lens(
                id = "galaxy-s25-ultra-front",
                brand = "Samsung",
                model = "Front Camera",
                focalLength = 2.7,
                focalLength35mm = 26.0,
                aperture = 2.2,
                aperture35mm = 12.0,
                cropFactor = 5.4,
                facing = Facing.FRONT,
                pixelPitchUm = 1.12,
                resolution = 12.0,
                activeResolution = 12.0,
                afZones = 0,
                ois = Stabilization.NONE,
            ),
        ),
    ),
)

val Phone.displayName: String
    get() = "$brand $model"

val Phone.focalLengthSummary: String
    get() = lenses
        .map(Lens::focalLength35mm)
        .distinct()
        .sorted()
        .joinToString(", ") { it.toCompactString() }

fun Double.toCompactString(): String =
    BigDecimal.valueOf(this).stripTrailingZeros().toPlainString()

fun List<Phone>.filterByQuery(query: String): List<Phone> {
    val tokens = query.trim().lowercase().split(Regex("\\s+")).filter(String::isNotEmpty)
    if (tokens.isEmpty()) return this

    return filter { phone ->
        val searchableText = "${phone.brand} ${phone.model}".lowercase()
        tokens.all(searchableText::contains)
    }
}

private fun dateOf(year: Int, month: Int, day: Int) =
    GregorianCalendar(year, month, day).time
