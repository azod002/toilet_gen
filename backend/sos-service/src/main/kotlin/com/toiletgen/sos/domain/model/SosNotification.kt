package com.toiletgen.sos.domain.model

import java.time.Instant
import java.util.UUID

data class SosNotification(
    val id: UUID = UUID.randomUUID(),
    val requestId: UUID,
    val ownerId: UUID,
    val status: NotificationStatus = NotificationStatus.PENDING,
    val sentAt: Instant = Instant.now(),
)

enum class NotificationStatus { PENDING, ACCEPTED, DECLINED }
