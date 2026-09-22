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
                facing = when (lensDto.facing.lowercase()) {
                    "front" -> Facing.FRONT
                    "back", "rear" -> Facing.BACK
                    else -> Facing.OTHER
                },
                pixelPitchUm = lensDto.pixelPitchUm.toFloat(),
                resolution = lensDto.resolutionMp.toFloat(),
                activeResolution = lensDto.activeResolutionMp.toFloat(),
                afZones = lensDto.afZones,
                stabilization = when (lensDto.ois.lowercase()) {
                    "ois", "optical" -> Stabilization.OIS
                    "sensorshift", "sensor-shift" -> Stabilization.SENSORSHIFT
                    else -> Stabilization.NONE
                },
                type = when (lensDto.type.lowercase()) {
                    "wide" -> LensType.WIDE
                    "ultrawide", "ultra-wide" -> LensType.ULTRAWIDE
                    "telephoto" -> LensType.TELEPHOTO
                    "macro" -> LensType.MACRO
                    else -> LensType.WIDE
                },
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
