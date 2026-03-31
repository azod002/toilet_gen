package com.toiletgen.core.domain.usecase

import com.toiletgen.core.domain.repository.ToiletRepository

class GetNearbyToiletsUseCase(private val repository: ToiletRepository) {
    operator fun invoke(lat: Double, lon: Double, radius: Double = 1000.0) =
        repository.getNearbyToilets(lat, lon, radius)
}
