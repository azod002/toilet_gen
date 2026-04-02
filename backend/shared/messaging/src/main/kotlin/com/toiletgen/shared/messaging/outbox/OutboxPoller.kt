package com.toiletgen.shared.messaging.outbox

import com.toiletgen.shared.messaging.EventPublisher
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import java.time.Instant

/**
 * Polls the outbox table for unpublished events and relays them to Kafka.
 * Implements the "Polling Publisher" variant of the Transactional Outbox pattern.
 *
 * Guarantees:
 * - At-least-once delivery: events may be published more than once if the process
 *   crashes between Kafka send and marking as published. Consumers must be idempotent.
 * - Ordering: events are published in createdAt order per aggregate.
 */
class OutboxPoller(
    private val publisher: EventPublisher,
    private val pollIntervalMs: Long = 1000L,
    private val batchSize: Int = 100,
) {
    private val log = LoggerFactory.getLogger(OutboxPoller::class.java)
    private var job: Job? = null

    fun start(scope: CoroutineScope) {
        job = scope.launch {
            log.info("Outbox poller started (interval=${pollIntervalMs}ms, batch=$batchSize)")
            while (isActive) {
                try {
                    val published = pollAndPublish()
                    if (published == 0) {
                        delay(pollIntervalMs)
                    }
                    // If we published events, immediately poll again (drain mode)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    log.error("Outbox poller error: ${e.message}", e)
                    delay(pollIntervalMs * 5) // Back off on errors
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        log.info("Outbox poller stopped")
    }

    private suspend fun pollAndPublish(): Int {
        val events = transaction {
            OutboxTable.selectAll()
                .where { OutboxTable.published eq false }
                .orderBy(OutboxTable.createdAt)
                .limit(batchSize)
                .map { row ->
                    OutboxEvent(
                        id = row[OutboxTable.id],
                        topic = row[OutboxTable.topic],
                        aggregateId = row[OutboxTable.aggregateId],
                        eventType = row[OutboxTable.eventType],
                        payload = row[OutboxTable.payload],
                    )
                }
        }

        for (event in events) {
            try {
                publisher.publishRaw(event.topic, event.aggregateId, event.payload)
                transaction {
                    OutboxTable.update({ OutboxTable.id eq event.id }) {
                        it[published] = true
                        it[publishedAt] = Instant.now()
                    }
                }
            } catch (e: Exception) {
                log.error("Failed to publish outbox event ${event.id} (${event.eventType}): ${e.message}")
                break // Stop processing to preserve ordering
            }
        }

        return events.size
    }

    private data class OutboxEvent(
        val id: java.util.UUID,
        val topic: String,
        val aggregateId: String,
        val eventType: String,
        val payload: String,
    )
}
