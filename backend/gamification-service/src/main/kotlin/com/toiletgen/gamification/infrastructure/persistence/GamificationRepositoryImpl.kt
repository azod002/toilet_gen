package com.toiletgen.gamification.infrastructure.persistence

import com.toiletgen.gamification.domain.model.*
import com.toiletgen.gamification.domain.repository.GamificationRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class GamificationRepositoryImpl : GamificationRepository {

    override suspend fun getAllAchievements(): List<AchievementCatalog> = newSuspendedTransaction {
        AchievementsCatalogTable.selectAll().map {
            AchievementCatalog(
                id = it[AchievementsCatalogTable.id], name = it[AchievementsCatalogTable.name],
                description = it[AchievementsCatalogTable.description], icon = it[AchievementsCatalogTable.icon],
                conditionType = it[AchievementsCatalogTable.conditionType], threshold = it[AchievementsCatalogTable.threshold],
            )
        }
    }

    override suspend fun getUserAchievements(userId: UUID): List<UserAchievement> = newSuspendedTransaction {
        UserAchievementsTable.selectAll().where { UserAchievementsTable.userId eq userId }.map {
            UserAchievement(
                id = it[UserAchievementsTable.id], userId = it[UserAchievementsTable.userId],
                achievementId = it[UserAchievementsTable.achievementId], unlockedAt = it[UserAchievementsTable.unlockedAt],
            )
        }
    }

    override suspend fun unlockAchievement(achievement: UserAchievement) = newSuspendedTransaction {
        UserAchievementsTable.insert {
            it[id] = achievement.id; it[userId] = achievement.userId
            it[achievementId] = achievement.achievementId; it[unlockedAt] = achievement.unlockedAt
        }
        Unit
    }

    override suspend fun hasAchievement(userId: UUID, achievementId: UUID): Boolean = newSuspendedTransaction {
        UserAchievementsTable.selectAll().where {
            (UserAchievementsTable.userId eq userId) and (UserAchievementsTable.achievementId eq achievementId)
        }.count() > 0
    }

    override suspend fun getOrCreateStats(userId: UUID, year: Int): UserStats = newSuspendedTransaction {
        val existing = UserStatsTable.selectAll().where {
            (UserStatsTable.userId eq userId) and (UserStatsTable.year eq year)
        }.singleOrNull()
        if (existing != null) {
            existing.toStats()
        } else {
            UserStatsTable.insert {
                it[UserStatsTable.userId] = userId; it[UserStatsTable.year] = year
            }
            UserStats(userId = userId, year = year)
        }
    }

    override suspend fun incrementStat(userId: UUID, year: Int, field: String) = newSuspendedTransaction {
        // Ensure row exists
        val exists = UserStatsTable.selectAll().where {
            (UserStatsTable.userId eq userId) and (UserStatsTable.year eq year)
        }.count() > 0
        if (!exists) {
            UserStatsTable.insert { it[UserStatsTable.userId] = userId; it[UserStatsTable.year] = year }
        }
        val col = when (field) {
            "reviews_written" -> UserStatsTable.reviewsWritten
            "toilets_created" -> UserStatsTable.toiletsCreated
            "sos_sent" -> UserStatsTable.sosSent
            "sos_helped" -> UserStatsTable.sosHelped
            "toilets_visited" -> UserStatsTable.toiletsVisited
            else -> throw IllegalArgumentException("Unknown stat field: $field")
        }
        UserStatsTable.update({
            (UserStatsTable.userId eq userId) and (UserStatsTable.year eq year)
        }) {
            with(SqlExpressionBuilder) { it[col] = col + 1 }
        }
        Unit
    }

    override suspend fun getStats(userId: UUID, year: Int): UserStats? = newSuspendedTransaction {
        UserStatsTable.selectAll().where {
            (UserStatsTable.userId eq userId) and (UserStatsTable.year eq year)
        }.singleOrNull()?.toStats()
    }

    override suspend fun createAchievement(achievement: AchievementCatalog): AchievementCatalog = newSuspendedTransaction {
        AchievementsCatalogTable.insert {
            it[id] = achievement.id; it[name] = achievement.name
            it[description] = achievement.description; it[icon] = achievement.icon
            it[conditionType] = achievement.conditionType; it[threshold] = achievement.threshold
        }
        achievement
    }

    private fun ResultRow.toStats() = UserStats(
        userId = this[UserStatsTable.userId], year = this[UserStatsTable.year],
        toiletsVisited = this[UserStatsTable.toiletsVisited], reviewsWritten = this[UserStatsTable.reviewsWritten],
        sosSent = this[UserStatsTable.sosSent], sosHelped = this[UserStatsTable.sosHelped],
        toiletsCreated = this[UserStatsTable.toiletsCreated],
    )
}
