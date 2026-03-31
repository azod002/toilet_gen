package com.toiletgen.app.data.repository

import com.toiletgen.core.common.Resource
import com.toiletgen.core.domain.model.SosRequest
import com.toiletgen.core.domain.model.SosStatus
import com.toiletgen.core.domain.repository.SosRepository
import com.toiletgen.core.network.api.SosApi
import com.toiletgen.core.network.model.CreateSosRequest
import com.toiletgen.core.network.model.SosRequestResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SosRepositoryImpl(
    private val sosApi: SosApi,
) : SosRepository {

    override suspend fun createSosRequest(lat: Double, lon: Double): Resource<SosRequest> {
        return try {
            val response = sosApi.createSosRequest(CreateSosRequest(lat, lon))
            Resource.Success(response.toDomain())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Ошибка SOS запроса")
        }
    }

    override fun observeSosStatus(requestId: String): Flow<SosRequest> = flow {
        while (true) {
            try {
                val response = sosApi.getSosStatus(requestId)
                emit(response.toDomain())
                if (response.status in listOf("ACCEPTED", "DECLINED", "EXPIRED")) break
            } catch (_: Exception) {}
            delay(2000)
        }
    }

    override suspend fun acceptSos(requestId: String): Resource<Unit> {
        return try {
            sosApi.acceptSos(requestId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Ошибка")
        }
    }

    override suspend fun declineSos(requestId: String): Resource<Unit> {
        return try {
            sosApi.declineSos(requestId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Ошибка")
        }
    }
}

private fun SosRequestResponse.toDomain() = SosRequest(
    id = id, userId = userId, latitude = latitude, longitude = longitude,
    status = SosStatus.valueOf(status), matchedToiletId = matchedToiletId, createdAt = createdAt,
)
