package com.toiletgen.gamification.domain.repository

import com.toiletgen.gamification.domain.model.AchievementCatalog
import com.toiletgen.gamification.domain.model.UserAchievement
import com.toiletgen.gamification.domain.model.UserStats
import java.util.UUID

interface GamificationRepository {
    suspend fun getAllAchievements(): List<AchievementCatalog>
    suspend fun getUserAchievements(userId: UUID): List<UserAchievement>
    suspend fun unlockAchievement(achievement: UserAchievement)
    suspend fun hasAchievement(userId: UUID, achievementId: UUID): Boolean
    suspend fun getOrCreateStats(userId: UUID, year: Int): UserStats
    suspend fun incrementStat(userId: UUID, year: Int, field: String)
    suspend fun getStats(userId: UUID, year: Int): UserStats?
    suspend fun createAchievement(achievement: AchievementCatalog): AchievementCatalog
}
