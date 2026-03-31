package com.toiletgen.sos.domain.repository

import com.toiletgen.sos.domain.model.SosNotification
import com.toiletgen.sos.domain.model.SosRequest
import com.toiletgen.sos.domain.model.SosStatus
import java.util.UUID

interface SosRepository {
    suspend fun createRequest(request: SosRequest): SosRequest
    suspend fun findRequestById(id: UUID): SosRequest?
    suspend fun updateRequestStatus(id: UUID, status: SosStatus, matchedToiletId: UUID? = null)
    suspend fun createNotification(notification: SosNotification): SosNotification
    suspend fun findNotificationsByRequestId(requestId: UUID): List<SosNotification>
    suspend fun updateNotificationStatus(id: UUID, status: com.toiletgen.sos.domain.model.NotificationStatus)
}
