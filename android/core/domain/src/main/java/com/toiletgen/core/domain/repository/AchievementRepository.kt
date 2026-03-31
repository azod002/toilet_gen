package com.toiletgen.core.domain.repository

import com.toiletgen.core.common.Resource
import com.toiletgen.core.domain.model.Achievement
import kotlinx.coroutines.flow.Flow

interface AchievementRepository {
    fun getMyAchievements(): Flow<Resource<List<Achievement>>>
    fun getAllAchievements(): Flow<Resource<List<Achievement>>>
}
