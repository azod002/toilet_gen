package com.toiletgen.identity.domain.event

import com.toiletgen.shared.events.*
import com.toiletgen.shared.messaging.EventPublisher
import java.time.Instant
import java.util.UUID

class IdentityEventPublisher(private val publisher: EventPublisher) {

    suspend fun userRegistered(userId: String, username: String, email: String) {
        publisher.publish(
            EventTopics.IDENTITY,
            UserRegistered(
                eventId = UUID.randomUUID().toString(),
                occurredAt = Instant.now().toString(),
                aggregateId = userId,
                username = username,
                email = email,
            )
        )
    }

    suspend fun userProfileUpdated(userId: String, username: String, avatarUrl: String?) {
        publisher.publish(
            EventTopics.IDENTITY,
            UserProfileUpdated(
                eventId = UUID.randomUUID().toString(),
                occurredAt = Instant.now().toString(),
                aggregateId = userId,
                username = username,
                avatarUrl = avatarUrl,
            )
        )
    }
}
