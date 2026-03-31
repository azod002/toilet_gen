package com.toiletgen.toilet.domain.repository

import com.toiletgen.toilet.domain.model.Review
import java.util.UUID

interface ReviewRepository {
    suspend fun findByToiletId(toiletId: UUID): List<Review>
    suspend fun create(review: Review): Review
    suspend fun countByToiletId(toiletId: UUID): Int
    suspend fun avgRatingByToiletId(toiletId: UUID): Double
    suspend fun avgCleanlinessByToiletId(toiletId: UUID): Double
    suspend fun deleteByToiletId(toiletId: UUID)
}
