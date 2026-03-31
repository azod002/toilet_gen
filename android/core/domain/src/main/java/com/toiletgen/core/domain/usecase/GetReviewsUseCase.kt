package com.toiletgen.core.domain.usecase

import com.toiletgen.core.domain.repository.ReviewRepository

class GetReviewsUseCase(private val repository: ReviewRepository) {
    operator fun invoke(toiletId: String) = repository.getReviewsByToiletId(toiletId)
}
