package com.toiletgen.gamification.domain.model

import java.time.Instant
import java.util.UUID

data class AchievementCatalog(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val description: String,
    val icon: String,
    val conditionType: String,
    val threshold: Int,
)

data class UserAchievement(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val achievementId: UUID,
    val unlockedAt: Instant = Instant.now(),
)
