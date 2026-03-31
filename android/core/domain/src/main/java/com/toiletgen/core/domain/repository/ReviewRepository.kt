package com.toiletgen.core.domain.repository

import com.toiletgen.core.common.Resource
import com.toiletgen.core.domain.model.Review
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun getReviewsByToiletId(toiletId: String): Flow<Resource<List<Review>>>
    suspend fun addReview(review: Review): Resource<Review>
}
