package com.toiletgen.toilet.domain.event

import com.toiletgen.shared.events.*
import com.toiletgen.shared.messaging.EventPublisher
import java.time.Instant
import java.util.UUID

class ToiletEventPublisher(private val publisher: EventPublisher) {
    suspend fun toiletCreated(toilet: com.toiletgen.toilet.domain.model.Toilet) {
        publisher.publish(EventTopics.TOILET, ToiletCreated(
            eventId = UUID.randomUUID().toString(),
            occurredAt = Instant.now().toString(),
            aggregateId = toilet.id.toString(),
            ownerId = toilet.ownerId?.toString() ?: "",
            name = toilet.name,
            latitude = toilet.latitude,
            longitude = toilet.longitude,
            type = toilet.type.name,
        ))
    }

    suspend fun reviewAdded(review: com.toiletgen.toilet.domain.model.Review) {
        publisher.publish(EventTopics.TOILET, ReviewAdded(
            eventId = UUID.randomUUID().toString(),
            occurredAt = Instant.now().toString(),
            aggregateId = review.id.toString(),
            toiletId = review.toiletId.toString(),
            userId = review.userId.toString(),
            rating = review.rating,
        ))
    }

    suspend fun toiletVisited(toiletId: String, userId: String) {
        publisher.publish(EventTopics.TOILET, ToiletVisited(
            eventId = UUID.randomUUID().toString(),
            occurredAt = Instant.now().toString(),
            aggregateId = toiletId,
            toiletId = toiletId,
            userId = userId,
        ))
    }

    suspend fun ratingUpdated(toiletId: String, newAvg: Double) {
        publisher.publish(EventTopics.TOILET, RatingUpdated(
            eventId = UUID.randomUUID().toString(),
            occurredAt = Instant.now().toString(),
            aggregateId = toiletId,
            toiletId = toiletId,
            newAvgRating = newAvg,
        ))
    }
}
