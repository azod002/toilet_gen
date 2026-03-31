package com.toiletgen.core.domain.repository

import com.toiletgen.core.common.Resource
import com.toiletgen.core.domain.model.SosRequest
import kotlinx.coroutines.flow.Flow

interface SosRepository {
    suspend fun createSosRequest(lat: Double, lon: Double): Resource<SosRequest>
    fun observeSosStatus(requestId: String): Flow<SosRequest>
    suspend fun acceptSos(requestId: String): Resource<Unit>
    suspend fun declineSos(requestId: String): Resource<Unit>
}
