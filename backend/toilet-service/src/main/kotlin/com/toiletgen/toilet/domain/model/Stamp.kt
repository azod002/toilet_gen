package com.toiletgen.toilet.domain.model

import java.time.Instant
import java.util.UUID

data class UserStamp(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val toiletId: UUID,
    val obtainedAt: Instant = Instant.now(),
)

data class StampTrade(
    val id: UUID = UUID.randomUUID(),
    val senderId: UUID,
    val receiverId: UUID,
    val senderStampId: UUID,
    val receiverStampId: UUID,
    val status: TradeStatus = TradeStatus.PENDING,
    val createdAt: Instant = Instant.now(),
)

enum class TradeStatus { PENDING, ACCEPTED, DECLINED, CANCELLED }
