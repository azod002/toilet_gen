package com.toiletgen.gamification.application.handler

import com.toiletgen.gamification.domain.event.GamificationEventPublisher
import com.toiletgen.gamification.domain.model.UserAchievement
import com.toiletgen.gamification.domain.repository.GamificationRepository
import java.time.Year
import java.util.UUID

class GamificationHandler(
    private val repository: GamificationRepository,
    private val eventPublisher: GamificationEventPublisher,
) {
    suspend fun getAllAchievements() = repository.getAllAchievements()

    suspend fun getUserAchievements(userId: String): List<Pair<com.toiletgen.gamification.domain.model.AchievementCatalog, Boolean>> {
        val all = repository.getAllAchievements()
        val unlocked = repository.getUserAchievements(UUID.fromString(userId)).map { it.achievementId }.toSet()
        return all.map { it to (it.id in unlocked) }
    }

    suspend fun getYearlyReport(userId: String, year: Int): com.toiletgen.gamification.domain.model.UserStats {
        return repository.getOrCreateStats(UUID.fromString(userId), year)
    }

    suspend fun processReviewAdded(userId: String) {
        val uid = UUID.fromString(userId)
        val year = Year.now().value
        repository.incrementStat(uid, year, "reviews_written")
        checkAndUnlock(uid, "FIRST_REVIEW", 1, "reviews_written")
        checkAndUnlock(uid, "REVIEWER_10", 10, "reviews_written")
    }

    suspend fun processToiletCreated(ownerId: String) {
        val uid = UUID.fromString(ownerId)
        val year = Year.now().value
        repository.incrementStat(uid, year, "toilets_created")
        checkAndUnlock(uid, "CARTOGRAPHER", 1, "toilets_created")
    }

    suspend fun processToiletVisited(userId: String) {
        val uid = UUID.fromString(userId)
        val year = Year.now().value
        repository.incrementStat(uid, year, "toilets_visited")
        checkAndUnlock(uid, "FIRST_VISIT", 1, "toilets_visited")
        checkAndUnlock(uid, "VISITOR_10", 10, "toilets_visited")
    }

    suspend fun processSosRequested(userId: String) {
        val uid = UUID.fromString(userId)
        val year = Year.now().value
        repository.incrementStat(uid, year, "sos_sent")
        checkAndUnlock(uid, "FIRST_SOS", 1, "sos_sent")
    }

    suspend fun processSosAccepted(ownerId: String) {
        val uid = UUID.fromString(ownerId)
        val year = Year.now().value
        repository.incrementStat(uid, year, "sos_helped")
        checkAndUnlock(uid, "GOOD_SAMARITAN", 1, "sos_helped")
    }

    private suspend fun checkAndUnlock(userId: UUID, conditionType: String, threshold: Int, statField: String) {
        val achievements = repository.getAllAchievements().filter { it.conditionType == conditionType }
        val stats = repository.getOrCreateStats(userId, Year.now().value)
        val statValue = when (statField) {
            "reviews_written" -> stats.reviewsWritten
            "toilets_created" -> stats.toiletsCreated
            "sos_sent" -> stats.sosSent
            "sos_helped" -> stats.sosHelped
            "toilets_visited" -> stats.toiletsVisited
            else -> 0
        }
        for (achievement in achievements) {
            if (statValue >= achievement.threshold && !repository.hasAchievement(userId, achievement.id)) {
                val ua = UserAchievement(userId = userId, achievementId = achievement.id)
                repository.unlockAchievement(ua)
                eventPublisher.achievementUnlocked(userId.toString(), achievement.id.toString(), achievement.name)
            }
        }
    }
}
