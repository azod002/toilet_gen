package com.toiletgen.app.data.repository

import com.toiletgen.core.common.Resource
import com.toiletgen.core.common.networkBoundResource
import com.toiletgen.core.database.dao.ReviewDao
import com.toiletgen.core.database.entity.ReviewEntity
import com.toiletgen.core.domain.model.Review
import com.toiletgen.core.domain.repository.ReviewRepository
import com.toiletgen.core.network.api.ReviewApi
import com.toiletgen.core.network.model.CreateReviewRequest
import com.toiletgen.core.network.model.ReviewResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReviewRepositoryImpl(
    private val reviewApi: ReviewApi,
    private val reviewDao: ReviewDao,
) : ReviewRepository {

    override fun getReviewsByToiletId(toiletId: String): Flow<Resource<List<Review>>> {
        return networkBoundResource(
            query = { reviewDao.getByToiletId(toiletId).map { list -> list.map { it.toDomain() } } },
            fetch = { reviewApi.getReviews(toiletId) },
            saveFetchResult = { response -> reviewDao.upsertAll(response.map { it.toEntity() }) },
        )
    }

    override suspend fun addReview(review: Review): Resource<Review> {
        return try {
            val response = reviewApi.addReview(
                review.toiletId,
                CreateReviewRequest(review.rating, review.cleanlinessSmell, review.cleanlinessDirt, review.hasToiletPaper, review.comment),
            )
            reviewDao.upsert(response.toEntity())
            Resource.Success(response.toDomain())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Ошибка добавления отзыва")
        }
    }
}

private fun ReviewResponse.toEntity() = ReviewEntity(
    id = id, toiletId = toiletId, userId = userId, username = username,
    rating = rating, cleanlinessSmell = cleanlinessSmell, cleanlinessDirt = cleanlinessDirt,
    hasToiletPaper = hasToiletPaper, comment = comment, createdAt = createdAt,
)

private fun ReviewResponse.toDomain() = Review(
    id = id, toiletId = toiletId, userId = userId, username = username,
    rating = rating, cleanlinessSmell = cleanlinessSmell, cleanlinessDirt = cleanlinessDirt,
    hasToiletPaper = hasToiletPaper, comment = comment, createdAt = createdAt,
)

private fun ReviewEntity.toDomain() = Review(
    id = id, toiletId = toiletId, userId = userId, username = username,
    rating = rating, cleanlinessSmell = cleanlinessSmell, cleanlinessDirt = cleanlinessDirt,
    hasToiletPaper = hasToiletPaper, comment = comment, createdAt = createdAt,
)
