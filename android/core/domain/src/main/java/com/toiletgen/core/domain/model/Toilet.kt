package com.toiletgen.core.domain.model

data class Toilet(
    val id: String,
    val ownerId: String?,
    val name: String,
    val description: String,
    val type: ToiletType,
    val latitude: Double,
    val longitude: Double,
    val isPaid: Boolean,
    val price: Double?,
    val hasToiletPaper: Boolean,
    val avgRating: Double,
    val avgCleanliness: Double,
    val reviewCount: Int,
    val createdAt: Long,
)

enum class ToiletType {
    REGULAR,
    USER_ADDED,
    PAID,
    FREE,
    PRIVATE,
}
