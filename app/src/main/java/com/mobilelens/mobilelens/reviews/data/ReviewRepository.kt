package com.mobilelens.mobilelens.reviews.data

import com.mobilelens.mobilelens.core.data.remote.ApiClient
import com.mobilelens.mobilelens.core.data.remote.UploadApi
import com.mobilelens.mobilelens.core.data.remote.dtos.StorageUploadResponse
import com.mobilelens.mobilelens.reviews.data.remote.ReviewApi
import com.mobilelens.mobilelens.reviews.data.remote.dtos.CreateReviewRequest
import com.mobilelens.mobilelens.reviews.data.remote.dtos.ReviewDto
import com.mobilelens.mobilelens.reviews.data.remote.dtos.ReviewMediaInput
import com.mobilelens.mobilelens.reviews.model.Review
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.ConcurrentHashMap

class ReviewRepository(
    private val reviewApi: ReviewApi = ApiClient.createService(),
    private val uploadApi: UploadApi = ApiClient.createService()
) {
    suspend fun getReviewsForPhone(phoneId: String): List<Review> {
        val reviews = reviewApi.getReviews(phoneId).map(::mapToReview)
        reviews.forEach { reviewCache[it.id] = it }
        return reviews
    }

    // Served from memory when the review was already loaded as part of a thread
    suspend fun getReview(id: String): Review {
        reviewCache[id]?.let { return it }
        return mapToReview(reviewApi.getReview(id)).also { reviewCache[id] = it }
    }

    /** Uploads an image to the review media bucket; reference it in a review by its storage URL. */
    suspend fun uploadReviewImage(bytes: ByteArray, mimeType: String): StorageUploadResponse {
        // The backend picks the stored file's extension from the part's content type
        val body = bytes.toRequestBody(mimeType.toMediaType())
        return uploadApi.uploadReviewMedia(MultipartBody.Part.createFormData("file", "image", body))
    }

    /** New reviews are created as pending and only show up in threads once a moderator publishes them. */
    suspend fun createReview(
        phoneId: String,
        title: String,
        contentMarkdown: String,
        images: List<StorageUploadResponse>
    ): Review {
        val request = CreateReviewRequest(
            smartphoneId = phoneId,
            title = title,
            contentMarkdown = contentMarkdown,
            mediaItems = images.mapIndexed { index, image ->
                ReviewMediaInput(objectKey = image.objectKey, type = "photo", displayOrder = index)
            }
        )
        return mapToReview(reviewApi.createReview(request)).also { reviewCache[it.id] = it }
    }

    private fun mapToReview(dto: ReviewDto): Review {
        return Review(
            id = dto.id,
            title = dto.title,
            author = dto.authorId,
            content = dto.contentMarkdown,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
            commentCount = 0,
            likeCount = 0,
            assets = emptyList()
        )
    }

    private companion object {
        // Shared by all instances, since every screen's ViewModel creates its own repository
        val reviewCache = ConcurrentHashMap<String, Review>()
    }
}
