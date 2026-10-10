package com.mobilelens.mobilelens.phones.data

import com.mobilelens.mobilelens.core.data.remote.ApiClient
import com.mobilelens.mobilelens.phones.data.remote.PhoneApi
import com.mobilelens.mobilelens.phones.model.GalleryPhoto

// The backend caps a page at 50 photos
private const val PAGE_SIZE = 50

// The gallery shows everything at once, so paging stops here rather than walking a huge phone's photos
private const val MAX_PAGES = 4

/** Verified photos taken with a phone's cameras, from `GET api/smartphones/{id}/photos`. */
class PhoneGalleryRepository(
    private val phoneApi: PhoneApi = ApiClient.createService()
) {
    suspend fun getPhotos(phoneId: String): List<GalleryPhoto> {
        val photos = mutableListOf<GalleryPhoto>()
        for (page in 1..MAX_PAGES) {
            val response = phoneApi.getPhotos(phoneId, page = page, limit = PAGE_SIZE)
            response.data.mapNotNullTo(photos) { dto ->
                dto.url?.let { GalleryPhoto(id = dto.id, imageUrl = it) }
            }
            if (response.data.size < PAGE_SIZE) break
        }
        return photos
    }
}
