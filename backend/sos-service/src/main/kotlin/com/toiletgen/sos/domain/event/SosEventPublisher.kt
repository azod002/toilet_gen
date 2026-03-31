package com.toiletgen.sos.domain.event

import com.toiletgen.shared.events.*
import com.toiletgen.shared.messaging.EventPublisher
import java.time.Instant
import java.util.UUID

class SosEventPublisher(private val publisher: EventPublisher) {
    suspend fun sosRequested(requestId: String, userId: String, lat: Double, lon: Double) {
        publisher.publish(EventTopics.SOS, SOSRequested(
            eventId = UUID.randomUUID().toString(), occurredAt = Instant.now().toString(),
            aggregateId = requestId, userId = userId, latitude = lat, longitude = lon,
        ))
    }

    suspend fun sosAccepted(requestId: String, ownerId: String, toiletId: String) {
        publisher.publish(EventTopics.SOS, SOSAccepted(
            eventId = UUID.randomUUID().toString(), occurredAt = Instant.now().toString(),
            aggregateId = requestId, requestId = requestId, ownerId = ownerId, toiletId = toiletId,
        ))
    }

    suspend fun sosDeclined(requestId: String, ownerId: String) {
        publisher.publish(EventTopics.SOS, SOSDeclined(
            eventId = UUID.randomUUID().toString(), occurredAt = Instant.now().toString(),
            aggregateId = requestId, requestId = requestId, ownerId = ownerId,
        ))
    }
}
