package com.toiletgen.shared.events

import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
sealed interface DomainEvent {
    val eventId: String
    val occurredAt: String
    val aggregateId: String
}
