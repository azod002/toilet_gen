package com.toiletgen.core.domain.model

data class SosRequest(
    val id: String,
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val status: SosStatus,
    val matchedToiletId: String?,
    val createdAt: Long,
)

enum class SosStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    EXPIRED,
}
