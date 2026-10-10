package com.mobilelens.mobilelens.reviews.data

import com.mobilelens.mobilelens.core.data.remote.ApiClient
import com.mobilelens.mobilelens.core.data.remote.UploadApi
import com.mobilelens.mobilelens.core.data.remote.dtos.StorageUploadResponse
import com.mobilelens.mobilelens.reviews.data.remote.ReviewApi
import com.mobilelens.mobilelens.reviews.data.remote.dtos.CreateCommentRequest
import com.mobilelens.mobilelens.reviews.data.remote.dtos.CreateReviewRequest
import com.mobilelens.mobilelens.reviews.data.remote.dtos.LikeResponse
import com.mobilelens.mobilelens.reviews.data.remote.dtos.ReviewCommentDto
import com.mobilelens.mobilelens.reviews.data.remote.dtos.ReviewDto
import com.mobilelens.mobilelens.reviews.data.remote.dtos.ReviewMediaInput
import com.mobilelens.mobilelens.reviews.model.Review
import com.mobilelens.mobilelens.reviews.model.ReviewComment
import com.mobilelens.mobilelens.reviews.model.ReviewAsset
import com.mobilelens.mobilelens.reviews.model.ReviewAssetType
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

    /** The signed-in user's reviews, including ones still in moderation or hidden. */
    suspend fun getMyReviews(): List<Review> {
        val reviews = reviewApi.getMyReviews().map(::mapToReview)
        reviews.forEach { reviewCache[it.id] = it }
        return reviews
    }

    /** Deletes the review with its media, comments and likes. Only its author or an admin may. */
    suspend fun deleteReview(id: String) {
        reviewApi.deleteReview(id)
        reviewCache.remove(id)
    }

    /** The review as last loaded by any screen, to show at once while [getReview] refreshes it. */
    fun getCachedReview(id: String): Review? = reviewCache[id]

    /** Always asks the backend: likes, comments and the viewer's own like change under the cache. */
    suspend fun getReview(id: String): Review =
        mapToReview(reviewApi.getReview(id)).also { reviewCache[id] = it }

    /** Every comment of a published review, oldest first. */
    suspend fun getComments(reviewId: String): List<ReviewComment> {
        val comments = mutableListOf<ReviewComment>()
        var page = 1
        while (true) {
            val response = reviewApi.getComments(reviewId, page = page, limit = COMMENTS_PAGE_SIZE)
            comments += response.data.map(::mapToComment)
            if (response.data.size < COMMENTS_PAGE_SIZE) break
            page++
        }
        reviewCache.computeIfPresent(reviewId) { _, cached -> cached.copy(commentCount = comments.size) }
        return comments
    }

    suspend fun addComment(reviewId: String, content: String): ReviewComment {
        val comment = mapToComment(reviewApi.createComment(reviewId, CreateCommentRequest(content.trim())))
        reviewCache.computeIfPresent(reviewId) { _, cached -> cached.copy(commentCount = cached.commentCount + 1) }
        return comment
    }

    suspend fun deleteComment(reviewId: String, commentId: String) {
        reviewApi.deleteComment(commentId)
        reviewCache.computeIfPresent(reviewId) { _, cached ->
            cached.copy(commentCount = (cached.commentCount - 1).coerceAtLeast(0))
        }
    }

    /** Likes or unlikes a published review and returns its new like count. */
    suspend fun setLiked(reviewId: String, liked: Boolean): LikeResponse {
        val response = if (liked) reviewApi.likeReview(reviewId) else reviewApi.unlikeReview(reviewId)
        reviewCache.computeIfPresent(reviewId) { _, cached ->
            cached.copy(likedByMe = response.liked, likeCount = response.likeCount)
        }
        return response
    }

    /**
     * Uploads an image to the review media bucket. The file stays private until its review is
     * published; use the response's `url` for a preview and attach it to the review by `objectKey`.
     */
    suspend fun uploadReviewImage(bytes: ByteArray, mimeType: String): StorageUploadResponse {
        // The backend picks the stored file's extension from the part's content type
        val body = bytes.toRequestBody(mimeType.toMediaType())
        return uploadApi.uploadReviewMedia(MultipartBody.Part.createFormData("file", "image", body))
    }

    /**
     * A review from a regular user is created as `pending` and only shows up in threads once a
     * moderator publishes it; reviewers and above are published straight away. Check [Review.status].
     */
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

    private fun mapToComment(dto: ReviewCommentDto) = ReviewComment(
        id = dto.id,
        authorId = dto.authorId,
        author = dto.authorName ?: dto.authorId,
        content = dto.content,
        createdAt = dto.createdAt,
    )

    private fun mapToReview(dto: ReviewDto): Review {
        return Review(
            id = dto.id,
            title = dto.title,
            author = dto.authorName ?: dto.authorId,
            content = dto.contentMarkdown,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
            commentCount = dto.commentCount,
            likeCount = dto.likeCount,
            likedByMe = dto.likedByMe,
            assets = dto.media.map { media ->
                ReviewAsset(
                    id = media.id,
                    type = if (media.type == "video") ReviewAssetType.VIDEO else ReviewAssetType.IMAGE,
                    storageUrl = media.url ?: media.storageUrl,
                )
            },
            status = dto.status
        )
    }

    private companion object {
        const val COMMENTS_PAGE_SIZE = 100

        // Shared by all instances, since every screen's ViewModel creates its own repository
        val reviewCache = ConcurrentHashMap<String, Review>()
    }
}
