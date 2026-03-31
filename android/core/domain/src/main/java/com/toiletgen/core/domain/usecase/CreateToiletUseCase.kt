package com.toiletgen.core.domain.usecase

import com.toiletgen.core.domain.model.Toilet
import com.toiletgen.core.domain.repository.ToiletRepository

class CreateToiletUseCase(private val repository: ToiletRepository) {
    suspend operator fun invoke(toilet: Toilet) = repository.createToilet(toilet)
}
