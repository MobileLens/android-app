package com.mobilelens.mobilelens.favorites.data

import com.mobilelens.mobilelens.core.data.remote.ApiClient
import com.mobilelens.mobilelens.favorites.data.remote.FavoriteApi
import com.mobilelens.mobilelens.favorites.data.remote.dtos.FavoriteDto
import com.mobilelens.mobilelens.phones.model.DeviceInfo
import com.mobilelens.mobilelens.phones.model.Phone

class FavoriteRepository(
    private val favoriteApi: FavoriteApi = ApiClient.createService(),
) {
    suspend fun getFavorites(): List<Phone> =
        favoriteApi.getFavorites().map(::toPhone)

    suspend fun addFavorite(smartphoneId: String) {
        favoriteApi.addFavorite(smartphoneId)
    }

    suspend fun removeFavorite(smartphoneId: String) {
        favoriteApi.removeFavorite(smartphoneId)
    }

    private fun toPhone(dto: FavoriteDto): Phone = Phone(
        id = dto.smartphoneId,
        deviceInfo = DeviceInfo(
            brand = dto.brandName.orEmpty(),
            model = dto.modelName.orEmpty(),
            imageURL = dto.imageUrl,
            releaseDate = null,
        ),
        lenses = emptyList(),
    )
}
