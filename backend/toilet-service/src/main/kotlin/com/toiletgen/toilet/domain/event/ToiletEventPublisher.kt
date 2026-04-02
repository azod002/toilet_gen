package com.toiletgen.toilet.domain.event

import com.toiletgen.shared.events.*
import com.toiletgen.shared.messaging.outbox.OutboxEventPublisher
import java.time.Instant
import java.util.UUID

/**
 * Publishes toilet domain events via Transactional Outbox.
 * All store() calls must happen inside an Exposed transaction block
 * to guarantee atomicity with the domain operation.
 */
class ToiletEventPublisher(private val outbox: OutboxEventPublisher) {

    fun toiletCreated(toilet: com.toiletgen.toilet.domain.model.Toilet) {
        outbox.store(EventTopics.TOILET, ToiletCreated(
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

    fun reviewAdded(review: com.toiletgen.toilet.domain.model.Review) {
        outbox.store(EventTopics.TOILET, ReviewAdded(
            eventId = UUID.randomUUID().toString(),
            occurredAt = Instant.now().toString(),
            aggregateId = review.id.toString(),
            toiletId = review.toiletId.toString(),
            userId = review.userId.toString(),
            rating = review.rating,
        ))
    }

    fun toiletVisited(toiletId: String, userId: String) {
        outbox.store(EventTopics.TOILET, ToiletVisited(
            eventId = UUID.randomUUID().toString(),
            occurredAt = Instant.now().toString(),
            aggregateId = toiletId,
            toiletId = toiletId,
            userId = userId,
        ))
    }

    fun ratingUpdated(toiletId: String, newAvg: Double) {
        outbox.store(EventTopics.TOILET, RatingUpdated(
            eventId = UUID.randomUUID().toString(),
            occurredAt = Instant.now().toString(),
            aggregateId = toiletId,
            toiletId = toiletId,
            newAvgRating = newAvg,
        ))
    }
}
