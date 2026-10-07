package com.mobilelens.mobilelens.reviews.data

import com.mobilelens.mobilelens.core.data.remote.ApiClient
import com.mobilelens.mobilelens.reviews.data.remote.ReviewApi
import com.mobilelens.mobilelens.reviews.data.remote.dtos.ReviewDto
import com.mobilelens.mobilelens.reviews.model.Review
import java.util.concurrent.ConcurrentHashMap

class ReviewRepository(
    private val reviewApi: ReviewApi = ApiClient.createService()
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
