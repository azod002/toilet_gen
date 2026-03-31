package com.toiletgen.app.data.repository

import com.toiletgen.core.common.Resource
import com.toiletgen.core.common.networkBoundResource
import com.toiletgen.core.database.dao.ToiletDao
import com.toiletgen.core.database.entity.ToiletEntity
import com.toiletgen.core.domain.model.Toilet
import com.toiletgen.core.domain.model.ToiletType
import com.toiletgen.core.domain.repository.ToiletRepository
import com.toiletgen.core.network.api.ToiletApi
import com.toiletgen.core.network.model.CreateToiletRequest
import com.toiletgen.core.network.model.ToiletResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class ToiletRepositoryImpl(
    private val toiletApi: ToiletApi,
    private val toiletDao: ToiletDao,
) : ToiletRepository {

    override fun getNearbyToilets(lat: Double, lon: Double, radius: Double): Flow<Resource<List<Toilet>>> {
        val delta = radius / 111.0
        return networkBoundResource(
            query = { toiletDao.getNearby(lat - delta, lat + delta, lon - delta, lon + delta).map { list -> list.map { it.toDomain() } } },
            fetch = { toiletApi.getNearbyToilets(lat, lon, radius) },
            saveFetchResult = { response ->
                toiletDao.deleteNearby(lat - delta, lat + delta, lon - delta, lon + delta)
                toiletDao.upsertAll(response.map { it.toEntity() })
            },
        )
    }

    override fun getToiletById(id: String): Flow<Resource<Toilet>> {
        return networkBoundResource(
            query = { toiletDao.getById(id).filter { it != null }.map { it!!.toDomain() } },
            fetch = { toiletApi.getToiletById(id) },
            saveFetchResult = { response -> toiletDao.upsert(response.toEntity()) },
        )
    }

    override suspend fun createToilet(toilet: Toilet): Resource<Toilet> {
        return try {
            val response = toiletApi.createToilet(CreateToiletRequest(
                name = toilet.name, description = toilet.description, type = toilet.type.name,
                latitude = toilet.latitude, longitude = toilet.longitude, isPaid = toilet.isPaid,
                price = toilet.price, hasToiletPaper = toilet.hasToiletPaper,
            ))
            toiletDao.upsert(response.toEntity())
            Resource.Success(response.toDomain())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Ошибка создания точки")
        }
    }

    override suspend fun updateToilet(toilet: Toilet): Resource<Toilet> {
        return try {
            val response = toiletApi.updateToilet(toilet.id, CreateToiletRequest(
                name = toilet.name, description = toilet.description, type = toilet.type.name,
                latitude = toilet.latitude, longitude = toilet.longitude, isPaid = toilet.isPaid,
                price = toilet.price, hasToiletPaper = toilet.hasToiletPaper,
            ))
            toiletDao.upsert(response.toEntity())
            Resource.Success(response.toDomain())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Ошибка обновления")
        }
    }
}

private fun ToiletResponse.toEntity() = ToiletEntity(
    id = id, ownerId = ownerId, name = name, description = description,
    type = type, latitude = latitude, longitude = longitude, isPaid = isPaid,
    price = price, hasToiletPaper = hasToiletPaper, avgRating = avgRating,
    avgCleanliness = avgCleanliness, reviewCount = reviewCount, createdAt = createdAt,
)

private fun ToiletResponse.toDomain() = Toilet(
    id = id, ownerId = ownerId, name = name, description = description,
    type = ToiletType.valueOf(type), latitude = latitude, longitude = longitude,
    isPaid = isPaid, price = price, hasToiletPaper = hasToiletPaper,
    avgRating = avgRating, avgCleanliness = avgCleanliness, reviewCount = reviewCount, createdAt = createdAt,
)

private fun ToiletEntity.toDomain() = Toilet(
    id = id, ownerId = ownerId, name = name, description = description,
    type = ToiletType.valueOf(type), latitude = latitude, longitude = longitude,
    isPaid = isPaid, price = price, hasToiletPaper = hasToiletPaper,
    avgRating = avgRating, avgCleanliness = avgCleanliness, reviewCount = reviewCount, createdAt = createdAt,
)
