package com.toiletgen.shared.messaging.outbox

import com.toiletgen.shared.events.DomainEvent
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insert
import java.time.Instant
import java.util.UUID

/**
 * Writes domain events to the outbox table within the current DB transaction.
 * This must be called inside an Exposed `transaction {}` block to guarantee
 * atomicity with the domain operation.
 *
 * Usage:
 *   transaction {
 *       toiletRepository.create(toilet)
 *       outbox.store("toilet-events", event)
 *   }
 */
class OutboxEventPublisher {

    private val json = Json { encodeDefaults = true }

    /**
     * Store an event in the outbox table. Must be called within an active Exposed transaction.
     */
    fun store(topic: String, event: DomainEvent) {
        val payload = json.encodeToString(event)
        OutboxTable.insert {
            it[id] = UUID.randomUUID()
            it[OutboxTable.topic] = topic
            it[aggregateId] = event.aggregateId
            it[eventType] = event::class.simpleName ?: "Unknown"
            it[OutboxTable.payload] = payload
            it[createdAt] = Instant.now()
            it[published] = false
            it[publishedAt] = null
        }
    }
}
