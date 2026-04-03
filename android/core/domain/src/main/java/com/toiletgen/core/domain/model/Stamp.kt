package com.toiletgen.core.domain.model

data class UserStamp(
    val id: String,
    val toiletId: String,
    val toiletName: String,
    val toiletType: String,
    val latitude: Double,
    val longitude: Double,
    val obtainedAt: Long,
)

data class StampTrade(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val senderStampId: String,
    val receiverStampId: String,
    val status: String,
    val createdAt: Long,
)
