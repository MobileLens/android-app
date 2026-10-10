package com.mobilelens.mobilelens.phones.data

import android.util.Log
import com.mobilelens.mobilelens.core.data.remote.ApiClient
import com.mobilelens.mobilelens.phones.data.remote.CameraApi
import com.mobilelens.mobilelens.phones.data.remote.PhoneApi
import com.mobilelens.mobilelens.phones.model.SubmittedCamera
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

private const val TAG = "MyCamerasRepository"

/** The cameras the signed-in user submitted, each with the name of the phone it belongs to. */
class MyCamerasRepository(
    private val cameraApi: CameraApi = ApiClient.createService(),
    private val phoneApi: PhoneApi = ApiClient.createService(),
) {
    suspend fun getMyCameras(): List<SubmittedCamera> = coroutineScope {
        val cameras = cameraApi.getMyCameras()
        // A user usually submits several lenses of one phone, so each phone is fetched once
        val names = cameras.map { it.smartphoneId }.distinct().map { phoneId ->
            async { phoneId to phoneName(phoneId) }
        }.awaitAll().toMap()
        cameras.map { dto ->
            SubmittedCamera(
                id = dto.id,
                phoneId = dto.smartphoneId,
                phoneName = names[dto.smartphoneId],
                type = lensTypeFromApi(dto.type),
                facing = facingFromApi(dto.facing),
                focalLengthMm = dto.focalLengthMm,
                resolutionMp = dto.resolutionMp,
                status = dto.status,
                submittedAt = dto.submittedAt,
            )
        }
    }

    // A phone that can't be read leaves its cameras unnamed rather than failing the whole list
    private suspend fun phoneName(phoneId: String): String? = try {
        phoneApi.getPhone(phoneId).modelName
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "Failed to read phone $phoneId", e)
        null
    }
}
