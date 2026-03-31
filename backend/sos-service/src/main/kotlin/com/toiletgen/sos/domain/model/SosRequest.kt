package com.toiletgen.sos.domain.model

import java.time.Instant
import java.util.UUID

data class SosRequest(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val latitude: Double,
    val longitude: Double,
    val status: SosStatus = SosStatus.PENDING,
    val matchedToiletId: UUID? = null,
    val createdAt: Instant = Instant.now(),
)

enum class SosStatus { PENDING, ACCEPTED, DECLINED, EXPIRED }
