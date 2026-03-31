package com.toiletgen.core.domain.usecase

import com.toiletgen.core.domain.model.Review
import com.toiletgen.core.domain.repository.ReviewRepository

class AddReviewUseCase(private val repository: ReviewRepository) {
    suspend operator fun invoke(review: Review) = repository.addReview(review)
}
