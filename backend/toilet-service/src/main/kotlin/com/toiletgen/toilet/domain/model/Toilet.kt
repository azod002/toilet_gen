package com.toiletgen.toilet.domain.model

import java.time.Instant
import java.util.UUID

data class Toilet(
    val id: UUID = UUID.randomUUID(),
    val ownerId: UUID? = null,
    val name: String,
    val description: String = "",
    val type: ToiletType,
    val latitude: Double,
    val longitude: Double,
    val isPaid: Boolean = false,
    val price: Double? = null,
    val hasToiletPaper: Boolean = true,
    val avgRating: Double = 0.0,
    val avgCleanliness: Double = 0.0,
    val reviewCount: Int = 0,
    val createdAt: Instant = Instant.now(),
)

enum class ToiletType { REGULAR, USER_ADDED, PAID, FREE, PRIVATE }
