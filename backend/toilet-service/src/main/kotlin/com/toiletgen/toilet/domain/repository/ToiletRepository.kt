package com.toiletgen.toilet.domain.repository

import com.toiletgen.toilet.domain.model.Toilet
import java.util.UUID

interface ToiletRepository {
    suspend fun findById(id: UUID): Toilet?
    suspend fun findNearby(lat: Double, lon: Double, radiusKm: Double): List<Toilet>
    suspend fun create(toilet: Toilet): Toilet
    suspend fun update(toilet: Toilet): Toilet
    suspend fun updateRating(toiletId: UUID, avgRating: Double, avgCleanliness: Double, reviewCount: Int)
    suspend fun delete(id: UUID)
}
