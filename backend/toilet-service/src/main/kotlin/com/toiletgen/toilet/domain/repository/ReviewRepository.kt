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

    /** In-transaction variants for use with Transactional Outbox */
    fun createInTransaction(review: Review): Review
    fun countByToiletIdInTransaction(toiletId: UUID): Int
    fun avgRatingByToiletIdInTransaction(toiletId: UUID): Double
    fun avgCleanlinessByToiletIdInTransaction(toiletId: UUID): Double
}
