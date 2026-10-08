package com.mobilelens.mobilelens.phones.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Material's image placeholder: a rounded triangle above a scalloped "cookie" and a tilted rounded
 * square, drawn in the largest square that fits.
 */
@Composable
fun PhotoPlaceholder(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
) {
    Spacer(
        modifier = modifier.drawWithCache {
            val side = size.minDimension
            val origin = Offset((size.width - side) / 2, (size.height - side) / 2)

            // Positions and sizes are fractions of the square's side
            fun at(x: Float, y: Float) = origin + Offset(x, y) * side

            val triangle = roundedPolygon(
                center = at(0.5f, 0.28f),
                radius = 0.28f * side,
                sides = 3,
                startDegrees = -90f,
                rounding = 0.35f,
            )
            val square = roundedPolygon(
                center = at(0.76f, 0.75f),
                radius = 0.3f * side,
                sides = 4,
                // 45° would be axis-aligned
                startDegrees = 50f,
                rounding = 0.2f,
            )
            val cookie = scallopedCircle(center = at(0.24f, 0.75f), radius = 0.22f * side, scallops = 9)

            onDrawBehind {
                drawPath(triangle, color)
                drawPath(cookie, color)
                drawPath(square, color)
            }
        }
    )
}

/**
 * A regular polygon around [center], with each corner cut [rounding] of the way along its edges
 * and bent into a curve.
 */
private fun roundedPolygon(
    center: Offset,
    radius: Float,
    sides: Int,
    startDegrees: Float,
    rounding: Float,
): Path {
    val corners = List(sides) { i ->
        val angle = Math.toRadians(startDegrees + i * 360.0 / sides)
        center + Offset(cos(angle).toFloat(), sin(angle).toFloat()) * radius
    }
    return Path().apply {
        corners.forEachIndexed { i, corner ->
            val start = lerp(corner, corners[(i + sides - 1) % sides], rounding)
            val end = lerp(corner, corners[(i + 1) % sides], rounding)
            if (i == 0) moveTo(start.x, start.y) else lineTo(start.x, start.y)
            quadraticTo(corner.x, corner.y, end.x, end.y)
        }
        close()
    }
}

/** A circle whose edge bulges out [scallops] times. */
private fun scallopedCircle(center: Offset, radius: Float, scallops: Int): Path = Path().apply {
    val steps = scallops * 16
    for (i in 0 until steps) {
        val angle = 2 * PI * i / steps
        val r = radius * (1 + 0.08 * cos(scallops * angle)).toFloat()
        val point = center + Offset(cos(angle).toFloat(), sin(angle).toFloat()) * r
        if (i == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
    }
    close()
}

@Preview(showBackground = true)
@Composable
private fun PhotoPlaceholderPreview() {
    MaterialTheme {
        PhotoPlaceholder(modifier = Modifier.size(120.dp))
    }
}
