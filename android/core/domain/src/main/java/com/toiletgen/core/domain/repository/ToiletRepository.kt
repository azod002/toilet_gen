package com.toiletgen.core.domain.repository

import com.toiletgen.core.common.Resource
import com.toiletgen.core.domain.model.Toilet
import kotlinx.coroutines.flow.Flow

interface ToiletRepository {
    fun getNearbyToilets(lat: Double, lon: Double, radius: Double): Flow<Resource<List<Toilet>>>
    fun getToiletById(id: String): Flow<Resource<Toilet>>
    suspend fun createToilet(toilet: Toilet): Resource<Toilet>
    suspend fun updateToilet(toilet: Toilet): Resource<Toilet>
}
