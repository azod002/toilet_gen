package com.toiletgen.core.domain.usecase

import com.toiletgen.core.domain.repository.SosRepository

class RequestSosUseCase(private val repository: SosRepository) {
    suspend operator fun invoke(lat: Double, lon: Double) =
        repository.createSosRequest(lat, lon)
}
