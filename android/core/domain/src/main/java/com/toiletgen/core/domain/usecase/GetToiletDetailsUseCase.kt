package com.toiletgen.core.domain.usecase

import com.toiletgen.core.domain.repository.ToiletRepository

class GetToiletDetailsUseCase(private val repository: ToiletRepository) {
    operator fun invoke(id: String) = repository.getToiletById(id)
}
