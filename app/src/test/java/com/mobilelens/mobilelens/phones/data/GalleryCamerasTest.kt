package com.mobilelens.mobilelens.phones.data

import com.mobilelens.mobilelens.phones.data.remote.dtos.LensDto
import com.mobilelens.mobilelens.phones.model.Facing
import com.mobilelens.mobilelens.phones.model.LensType
import org.junit.Assert.assertEquals
import org.junit.Test

class GalleryCamerasTest {
    private fun camera(
        id: String,
        phoneId: String = "phone",
        type: String = "wide",
        facing: String = "back",
        focalLengthMm: Double = 6.9,
        status: String = "approved",
    ) = LensDto(
        id = id,
        smartphoneId = phoneId,
        status = status,
        type = type,
        facing = facing,
        focalLengthMm = focalLengthMm,
        aperture = 1.7,
        cropFactor = 3.9,
        pixelPitchUm = 1.2,
        resolutionMp = 50.0,
        activeResolutionMp = 12.5,
    )

    @Test
    fun addsOwnCamerasOfThisPhoneInReview() {
        val cameras = galleryCamerasFrom(
            phoneId = "phone",
            listed = listOf(camera("listed")),
            mine = listOf(
                camera("mine", focalLengthMm = 2.0, type = "ultrawide", status = "pending"),
                camera("other phone", phoneId = "other"),
            ),
        )
        assertEquals(listOf("listed", "mine"), cameras.map { it.id })
        assertEquals("pending", cameras[1].status)
    }

    @Test
    fun leavesOutRejectedAndDuplicateCameras() {
        val cameras = galleryCamerasFrom(
            phoneId = "phone",
            listed = listOf(camera("approved")),
            mine = listOf(camera("approved"), camera("rejected", status = "rejected")),
        )
        assertEquals(listOf("approved"), cameras.map { it.id })
    }

    @Test
    fun listsBackCamerasFirstThenByTypeAndFocalLength() {
        val cameras = galleryCamerasFrom(
            phoneId = "phone",
            listed = listOf(
                camera("front", facing = "front", focalLengthMm = 2.5),
                camera("tele 5x", type = "tele", focalLengthMm = 18.0),
                camera("tele 3x", type = "tele", focalLengthMm = 9.0),
                camera("ultrawide", type = "ultrawide", focalLengthMm = 2.0),
                camera("wide"),
            ),
            mine = emptyList(),
        )
        assertEquals(
            listOf("wide", "ultrawide", "tele 3x", "tele 5x", "front"),
            cameras.map { it.id },
        )
        assertEquals(LensType.TELEPHOTO, cameras[2].type)
        assertEquals(Facing.FRONT, cameras.last().facing)
    }
}
