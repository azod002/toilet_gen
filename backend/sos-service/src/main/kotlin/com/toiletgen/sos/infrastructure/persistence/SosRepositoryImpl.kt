package com.toiletgen.sos.infrastructure.persistence

import com.toiletgen.sos.domain.model.*
import com.toiletgen.sos.domain.repository.SosRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class SosRepositoryImpl : SosRepository {

    override suspend fun createRequest(request: SosRequest): SosRequest = newSuspendedTransaction {
        SosRequestsTable.insert {
            it[id] = request.id; it[userId] = request.userId
            it[latitude] = request.latitude; it[longitude] = request.longitude
            it[status] = request.status.name; it[matchedToiletId] = request.matchedToiletId
            it[createdAt] = request.createdAt
        }
        request
    }

    override suspend fun findRequestById(id: UUID): SosRequest? = newSuspendedTransaction {
        SosRequestsTable.selectAll().where { SosRequestsTable.id eq id }.singleOrNull()?.let {
            SosRequest(
                id = it[SosRequestsTable.id], userId = it[SosRequestsTable.userId],
                latitude = it[SosRequestsTable.latitude], longitude = it[SosRequestsTable.longitude],
                status = SosStatus.valueOf(it[SosRequestsTable.status]),
                matchedToiletId = it[SosRequestsTable.matchedToiletId],
                createdAt = it[SosRequestsTable.createdAt],
            )
        }
    }

    override suspend fun updateRequestStatus(id: UUID, status: SosStatus, matchedToiletId: UUID?) = newSuspendedTransaction {
        SosRequestsTable.update({ SosRequestsTable.id eq id }) {
            it[SosRequestsTable.status] = status.name
            if (matchedToiletId != null) it[SosRequestsTable.matchedToiletId] = matchedToiletId
        }
        Unit
    }

    override suspend fun createNotification(notification: SosNotification): SosNotification = newSuspendedTransaction {
        SosNotificationsTable.insert {
            it[id] = notification.id; it[requestId] = notification.requestId
            it[ownerId] = notification.ownerId; it[status] = notification.status.name
            it[sentAt] = notification.sentAt
        }
        notification
    }

    override suspend fun findNotificationsByRequestId(requestId: UUID): List<SosNotification> = newSuspendedTransaction {
        SosNotificationsTable.selectAll().where { SosNotificationsTable.requestId eq requestId }.map {
            SosNotification(
                id = it[SosNotificationsTable.id], requestId = it[SosNotificationsTable.requestId],
                ownerId = it[SosNotificationsTable.ownerId],
                status = NotificationStatus.valueOf(it[SosNotificationsTable.status]),
                sentAt = it[SosNotificationsTable.sentAt],
            )
        }
    }

    override suspend fun updateNotificationStatus(id: UUID, status: NotificationStatus) = newSuspendedTransaction {
        SosNotificationsTable.update({ SosNotificationsTable.id eq id }) {
            it[SosNotificationsTable.status] = status.name
        }
        Unit
    }
}
