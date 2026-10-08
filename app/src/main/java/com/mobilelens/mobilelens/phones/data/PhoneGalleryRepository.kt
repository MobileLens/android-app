package com.mobilelens.mobilelens.phones.data

import com.mobilelens.mobilelens.phones.model.GalleryPhoto

/**
 * Photos taken with a phone's cameras.
 *
 * The backend keeps them in its `photo` table (one row per camera), but it has no endpoint that
 * lists them yet, and it stores their location as `minio://<bucket>/<objectKey>` rather than an HTTP
 * URL. Until both exist, every phone's gallery is empty.
 */
class PhoneGalleryRepository {
    // TODO: once the backend has `GET api/smartphones/{id}/photos` (verified photos, paged like
    //  `api/smartphones`), call it through a Retrofit `PhotoApi` and map each item's public `url`
    //  to `imageUrl`. Use that `url` rather than `storageUrl`, which is a `minio://` reference.
    suspend fun getPhotos(phoneId: String): List<GalleryPhoto> = emptyList()
}
