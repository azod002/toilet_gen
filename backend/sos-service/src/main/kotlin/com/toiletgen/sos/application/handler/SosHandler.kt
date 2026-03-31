package com.toiletgen.sos.application.handler

import com.toiletgen.sos.domain.event.SosEventPublisher
import com.toiletgen.sos.domain.model.*
import com.toiletgen.sos.domain.repository.SosRepository
import java.util.UUID

class SosHandler(
    private val sosRepository: SosRepository,
    private val eventPublisher: SosEventPublisher,
) {
    suspend fun createRequest(userId: String, lat: Double, lon: Double): SosRequest {
        val request = SosRequest(userId = UUID.fromString(userId), latitude = lat, longitude = lon)
        val saved = sosRepository.createRequest(request)
        eventPublisher.sosRequested(saved.id.toString(), userId, lat, lon)
        return saved
    }

    suspend fun getRequestStatus(requestId: String): SosRequest {
        return sosRepository.findRequestById(UUID.fromString(requestId))
            ?: throw IllegalArgumentException("SOS запрос не найден")
    }

    suspend fun acceptRequest(requestId: String, ownerId: String, toiletId: String) {
        val request = sosRepository.findRequestById(UUID.fromString(requestId))
            ?: throw IllegalArgumentException("SOS запрос не найден")
        sosRepository.updateRequestStatus(request.id, SosStatus.ACCEPTED, UUID.fromString(toiletId))
        eventPublisher.sosAccepted(requestId, ownerId, toiletId)
    }

    suspend fun declineRequest(requestId: String, ownerId: String) {
        val request = sosRepository.findRequestById(UUID.fromString(requestId))
            ?: throw IllegalArgumentException("SOS запрос не найден")
        // Check if all notifications declined -> mark request as declined
        val notifications = sosRepository.findNotificationsByRequestId(request.id)
        val allDeclined = notifications.all { it.status == NotificationStatus.DECLINED }
        if (allDeclined) {
            sosRepository.updateRequestStatus(request.id, SosStatus.DECLINED)
        }
        eventPublisher.sosDeclined(requestId, ownerId)
    }
}
