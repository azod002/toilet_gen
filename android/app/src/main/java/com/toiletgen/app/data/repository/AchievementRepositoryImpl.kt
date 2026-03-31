package com.toiletgen.app.data.repository

import com.toiletgen.core.common.Resource
import com.toiletgen.core.domain.model.Achievement
import com.toiletgen.core.domain.repository.AchievementRepository
import com.toiletgen.core.network.api.AchievementApi
import com.toiletgen.core.network.model.AchievementResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AchievementRepositoryImpl(
    private val achievementApi: AchievementApi,
) : AchievementRepository {

    override fun getMyAchievements(): Flow<Resource<List<Achievement>>> = flow {
        emit(Resource.Loading)
        try {
            val response = achievementApi.getMyAchievements()
            emit(Resource.Success(response.map { it.toDomain() }))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Ошибка загрузки ачивок"))
        }
    }

    override fun getAllAchievements(): Flow<Resource<List<Achievement>>> = flow {
        emit(Resource.Loading)
        try {
            val response = achievementApi.getAllAchievements()
            emit(Resource.Success(response.map { it.toDomain() }))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Ошибка загрузки"))
        }
    }
}

private fun AchievementResponse.toDomain() = Achievement(
    id = id, name = name, description = description, iconUrl = iconUrl,
    isUnlocked = isUnlocked, unlockedAt = unlockedAt,
)
