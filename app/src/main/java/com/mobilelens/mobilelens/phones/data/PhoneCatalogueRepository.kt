package com.mobilelens.mobilelens.phones.data

import com.mobilelens.mobilelens.core.data.remote.ApiClient
import com.mobilelens.mobilelens.phones.data.remote.PhoneApi
import com.mobilelens.mobilelens.phones.data.remote.dtos.PhoneDto
import com.mobilelens.mobilelens.phones.data.remote.BrandApi
import com.mobilelens.mobilelens.phones.model.DeviceInfo
import com.mobilelens.mobilelens.phones.model.Facing
import com.mobilelens.mobilelens.phones.model.Lens
import com.mobilelens.mobilelens.phones.model.LensType
import com.mobilelens.mobilelens.phones.model.Phone
import com.mobilelens.mobilelens.phones.model.Stabilization
import com.mobilelens.mobilelens.phones.model.VideoResolution
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

class PhoneCatalogueRepository {
    private val phoneApi = ApiClient.createService<PhoneApi>()
    private val brandApi = ApiClient.createService<BrandApi>()
    
    // Simple cache for brand names so we don't have to query the api repeatedly
    private val brandCache = ConcurrentHashMap<String, String>()

    private val _phones = MutableStateFlow<List<Phone>>(emptyList())
    val phones: StateFlow<List<Phone>> = _phones.asStateFlow()

    suspend fun loadPhones(query: String? = null) {
        val response = phoneApi.getPhones(query = query)
        val phoneList = response.data.map { listDto ->
            val fullPhoneDto = phoneApi.getPhone(listDto.id)
            mapToPhone(fullPhoneDto)
        }
        _phones.value = phoneList
    }

    suspend fun getPhoneById(id: String): Phone {
        val fullPhoneDto = phoneApi.getPhone(id)
        return mapToPhone(fullPhoneDto)
    }

    /** The catalogue phone whose model name is exactly [model] (ignoring case), if there is one. */
    suspend fun findPhoneByModel(model: String): Phone? {
        val match = phoneApi.getPhones(query = model).data
            .firstOrNull { it.modelName.equals(model, ignoreCase = true) }
            ?: return null
        return getPhoneById(match.id)
    }

    private suspend fun getBrandName(brandId: String): String {
        return brandCache.getOrPut(brandId) {
            try {
                brandApi.getBrand(brandId).name
            } catch (_: Exception) {
                // fallback to ID if brand fetch fails
                brandId
            }
        }
    }

    private suspend fun mapToPhone(dto: PhoneDto): Phone {
        val lenses = dto.cameras.map { lensDto ->
            Lens(
                focalLength = listOf(lensDto.focalLengthMm.toFloat()),
                aperture = listOf(lensDto.aperture.toFloat()),
                cropFactor = lensDto.cropFactor.toFloat(),
                sensorTypeDenominator = lensDto.cropFactor.toFloat() / 2.7f, // Approximate
                facing = facingFromApi(lensDto.facing),
                pixelPitchUm = lensDto.pixelPitchUm.toFloat(),
                resolution = lensDto.resolutionMp.toFloat(),
                activeResolution = lensDto.activeResolutionMp.toFloat(),
                afZones = lensDto.afZones,
                stabilization = stabilizationFromApi(lensDto.ois),
                type = lensTypeFromApi(lensDto.type),
                videoResolutions = lensDto.videoModes.map { vm ->
                    VideoResolution(vm.widthPx, vm.heightPx, vm.fpsMax.toInt())
                }
            )
        }

        return Phone(
            id = dto.id,
            deviceInfo = DeviceInfo(
                brand = getBrandName(dto.brandId),
                model = dto.modelName,
                releaseDate = dto.releaseDate,
                imageURL = dto.imageUrl
            ),
            lenses = lenses
        )
    }
}

// Backend values live in backend-api `camera` table (api/src/db/schema.ts).
// A few extra aliases are accepted on top of those.

internal fun lensTypeFromApi(value: String): LensType = when (value.lowercase()) {
    "wide" -> LensType.WIDE
    "ultrawide", "ultra-wide" -> LensType.ULTRAWIDE
    "tele", "telephoto" -> LensType.TELEPHOTO
    "macro" -> LensType.MACRO
    else -> LensType.OTHER
}

internal fun facingFromApi(value: String): Facing = when (value.lowercase()) {
    "front" -> Facing.FRONT
    "back", "rear" -> Facing.BACK
    else -> Facing.OTHER
}

internal fun stabilizationFromApi(value: String): Stabilization = when (value.lowercase()) {
    "optical", "ois" -> Stabilization.OIS
    "sensor_shift", "sensorshift", "sensor-shift" -> Stabilization.SENSORSHIFT
    else -> Stabilization.NONE
}
