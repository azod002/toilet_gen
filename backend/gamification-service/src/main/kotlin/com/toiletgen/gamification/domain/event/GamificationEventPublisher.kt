package com.toiletgen.gamification.domain.event

import com.toiletgen.shared.events.*
import com.toiletgen.shared.messaging.EventPublisher
import java.time.Instant
import java.util.UUID

class GamificationEventPublisher(private val publisher: EventPublisher) {
    suspend fun achievementUnlocked(userId: String, achievementId: String, achievementName: String) {
        publisher.publish(EventTopics.GAMIFICATION, AchievementUnlocked(
            eventId = UUID.randomUUID().toString(), occurredAt = Instant.now().toString(),
            aggregateId = userId, userId = userId, achievementId = achievementId,
            achievementName = achievementName,
        ))
    }
}
