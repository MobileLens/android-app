package com.mobilelens.mobilelens.phones.data

import com.mobilelens.mobilelens.phones.model.Facing
import com.mobilelens.mobilelens.phones.model.Lens
import com.mobilelens.mobilelens.phones.model.LensType
import com.mobilelens.mobilelens.phones.model.Stabilization
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class LensComparisonTest {

    private fun lens(
        facing: Facing = Facing.BACK,
        type: LensType = LensType.WIDE,
        focalLength: Float = 6.9f,
        cropFactor: Float = 3.5f,
    ) = Lens(
        focalLength = listOf(focalLength),
        aperture = listOf(1.8f),
        cropFactor = cropFactor,
        sensorTypeDenominator = 1.3f,
        facing = facing,
        pixelPitchUm = 1.2f,
        resolution = 50f,
        activeResolution = 12f,
        afZones = 1,
        stabilization = Stabilization.OIS,
        videoResolutions = emptyList(),
        type = type,
    )

    @Test
    fun pairLenses_matchesLensesOfTheSameKind() {
        val firstWide = lens(type = LensType.WIDE)
        val firstTele = lens(type = LensType.TELEPHOTO, focalLength = 15f)
        val secondTele = lens(type = LensType.TELEPHOTO, focalLength = 18f)
        val secondWide = lens(type = LensType.WIDE, focalLength = 7f)

        val pairs = pairLenses(listOf(firstTele, firstWide), listOf(secondWide, secondTele))

        assertEquals(listOf(LensType.WIDE, LensType.TELEPHOTO), pairs.map { it.type })
        assertSame(firstWide, pairs[0].first)
        assertSame(secondWide, pairs[0].second)
        assertSame(firstTele, pairs[1].first)
        assertSame(secondTele, pairs[1].second)
    }

    @Test
    fun pairLenses_leavesASideEmptyWhenOnlyOnePhoneHasTheLens() {
        val macro = lens(type = LensType.MACRO)

        val pairs = pairLenses(listOf(lens(), macro), listOf(lens()))

        assertEquals(2, pairs.size)
        assertSame(macro, pairs[1].first)
        assertNull(pairs[1].second)
    }

    @Test
    fun pairLenses_putsBackLensesFirstThenTypeOrder() {
        val pairs = pairLenses(
            listOf(
                lens(facing = Facing.FRONT, type = LensType.WIDE),
                lens(facing = Facing.OTHER, type = LensType.WIDE),
                lens(facing = Facing.BACK, type = LensType.TELEPHOTO),
            ),
            listOf(
                lens(facing = Facing.BACK, type = LensType.ULTRAWIDE),
                lens(facing = Facing.BACK, type = LensType.WIDE),
            ),
        )

        assertEquals(
            listOf(
                Facing.BACK to LensType.WIDE,
                Facing.BACK to LensType.ULTRAWIDE,
                Facing.BACK to LensType.TELEPHOTO,
                Facing.FRONT to LensType.WIDE,
                Facing.OTHER to LensType.WIDE,
            ),
            pairs.map { it.facing to it.type },
        )
    }

    @Test
    fun pairLenses_ordersSeveralLensesOfAKindByEquivalentFocalLength() {
        // 3x (~70 mm) and 5x (~120 mm) telephotos, listed long one first
        val tele5x = lens(type = LensType.TELEPHOTO, focalLength = 19f, cropFactor = 6.3f)
        val tele3x = lens(type = LensType.TELEPHOTO, focalLength = 10f, cropFactor = 7f)
        val otherTele = lens(type = LensType.TELEPHOTO, focalLength = 11f, cropFactor = 6.4f)

        val pairs = pairLenses(listOf(tele5x, tele3x), listOf(otherTele))

        assertEquals(listOf(0, 1), pairs.map { it.ordinal })
        assertSame(tele3x, pairs[0].first)
        assertSame(otherTele, pairs[0].second)
        assertSame(tele5x, pairs[1].first)
        assertNull(pairs[1].second)
    }

    @Test
    fun pairLenses_emptyWhenNeitherPhoneHasLenses() {
        assertEquals(emptyList<LensPair>(), pairLenses(emptyList(), emptyList()))
    }

    @Test
    fun betterSide_followsTheDirection() {
        assertEquals(ComparedSide.SECOND, betterSide(48f, 50f, decimals = 0, higherIsBetter = true))
        assertEquals(ComparedSide.FIRST, betterSide(1.7f, 2.2f, decimals = 1, higherIsBetter = false))
    }

    @Test
    fun betterSide_tiesValuesThatLookTheSame() {
        // f/1.78 and f/1.8 both show as f/1.8
        assertNull(betterSide(1.78f, 1.8f, decimals = 1, higherIsBetter = false))
        // 48.2 MP and 48 MP both show as 48 MP
        assertNull(betterSide(48.2f, 48f, decimals = 0, higherIsBetter = true))
    }

    @Test
    fun betterSide_nullWhenAValueIsMissing() {
        assertNull(betterSide(null, 50f, decimals = 0, higherIsBetter = true))
        assertNull(betterSide(50f, null, decimals = 0, higherIsBetter = true))
    }

    @Test
    fun betterStabilization_anyBeatsNoneButOisAndSensorShiftTie() {
        assertEquals(ComparedSide.FIRST, betterStabilization(Stabilization.OIS, Stabilization.NONE))
        assertEquals(ComparedSide.SECOND, betterStabilization(Stabilization.NONE, Stabilization.SENSORSHIFT))
        assertNull(betterStabilization(Stabilization.OIS, Stabilization.SENSORSHIFT))
        assertNull(betterStabilization(Stabilization.NONE, Stabilization.NONE))
        assertNull(betterStabilization(Stabilization.OIS, null))
    }
}
