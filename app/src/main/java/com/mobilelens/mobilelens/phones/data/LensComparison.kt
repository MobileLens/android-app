package com.mobilelens.mobilelens.phones.data

import com.mobilelens.mobilelens.phones.model.Facing
import com.mobilelens.mobilelens.phones.model.Lens
import com.mobilelens.mobilelens.phones.model.LensType
import com.mobilelens.mobilelens.phones.model.Stabilization
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * The lenses of one kind on two phones, side by side. [first] or [second] is null when that phone
 * has no such lens. [ordinal] tells apart several lenses of the same kind on one phone (e.g. a 3x
 * and a 5x telephoto), counting from the shortest focal length.
 */
data class LensPair(
    val facing: Facing,
    val type: LensType,
    val ordinal: Int,
    val first: Lens?,
    val second: Lens?,
)

/** The side of a comparison whose value comes out ahead. */
enum class ComparedSide { FIRST, SECOND }

// Back cameras first, since they are the ones people compare
private val FacingOrder = listOf(Facing.BACK, Facing.FRONT, Facing.OTHER)

/**
 * Lines up two phones' lenses by facing and type, so wide is compared with wide and telephoto with
 * telephoto. Several lenses of one kind are paired in order of their 35mm-equivalent focal length.
 * Back lenses come first, then front ones, each in [LensType] order.
 */
fun pairLenses(first: List<Lens>, second: List<Lens>): List<LensPair> {
    val firstByKind = first.groupByKind()
    val secondByKind = second.groupByKind()
    return (firstByKind.keys + secondByKind.keys)
        .sortedWith(compareBy({ FacingOrder.indexOf(it.first) }, { it.second.ordinal }))
        .flatMap { kind ->
            val firstLenses = firstByKind[kind].orEmpty()
            val secondLenses = secondByKind[kind].orEmpty()
            List(maxOf(firstLenses.size, secondLenses.size)) { index ->
                LensPair(
                    facing = kind.first,
                    type = kind.second,
                    ordinal = index,
                    first = firstLenses.getOrNull(index),
                    second = secondLenses.getOrNull(index),
                )
            }
        }
}

private fun List<Lens>.groupByKind(): Map<Pair<Facing, LensType>, List<Lens>> =
    groupBy { it.facing to it.type }
        .mapValues { (_, lenses) -> lenses.sortedBy { it.mainFocalLength35mm } }

/**
 * The side with the better reading, or null when they are even or either is missing. Both are
 * first rounded to the [decimals] shown on screen, so two values that read the same never have a
 * winner.
 */
fun betterSide(
    first: Float?,
    second: Float?,
    decimals: Int,
    higherIsBetter: Boolean,
): ComparedSide? {
    if (first == null || second == null) return null
    val scale = 10.0.pow(decimals)
    val firstShown = (first * scale).roundToLong()
    val secondShown = (second * scale).roundToLong()
    return when {
        firstShown == secondShown -> null
        (firstShown > secondShown) == higherIsBetter -> ComparedSide.FIRST
        else -> ComparedSide.SECOND
    }
}

/** Any stabilization beats none; OIS and sensor-shift count as even. */
fun betterStabilization(first: Stabilization?, second: Stabilization?): ComparedSide? =
    betterSide(first?.rank(), second?.rank(), decimals = 0, higherIsBetter = true)

private fun Stabilization.rank(): Float = if (this == Stabilization.NONE) 0f else 1f
