package com.toiletgen.shared.messaging.outbox

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

/**
 * Transactional Outbox table.
 * Events are written here in the same DB transaction as the domain operation,
 * then asynchronously relayed to Kafka by the OutboxPoller.
 */
object OutboxTable : Table("outbox_events") {
    val id = uuid("id")
    val topic = varchar("topic", 100)
    val aggregateId = varchar("aggregate_id", 100)
    val eventType = varchar("event_type", 100)
    val payload = text("payload")
    val createdAt = timestamp("created_at")
    val published = bool("published").default(false)
    val publishedAt = timestamp("published_at").nullable()
    override val primaryKey = PrimaryKey(id)
}
