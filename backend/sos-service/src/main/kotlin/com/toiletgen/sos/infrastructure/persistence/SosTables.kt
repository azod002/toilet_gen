package com.toiletgen.sos.infrastructure.persistence

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object SosRequestsTable : Table("sos_requests") {
    val id = uuid("id")
    val userId = uuid("user_id")
    val latitude = double("latitude")
    val longitude = double("longitude")
    val status = varchar("status", 20)
    val matchedToiletId = uuid("matched_toilet_id").nullable()
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object SosNotificationsTable : Table("sos_notifications") {
    val id = uuid("id")
    val requestId = uuid("request_id").references(SosRequestsTable.id)
    val ownerId = uuid("owner_id")
    val status = varchar("status", 20)
    val sentAt = timestamp("sent_at")
    override val primaryKey = PrimaryKey(id)
}
