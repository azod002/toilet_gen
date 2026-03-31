package com.toiletgen.toilet.domain.model

import java.time.Instant
import java.util.UUID

data class Review(
    val id: UUID = UUID.randomUUID(),
    val toiletId: UUID,
    val userId: UUID,
    val username: String,
    val rating: Int,
    val cleanlinessSmell: Int,
    val cleanlinessDirt: Int,
    val hasToiletPaper: Boolean,
    val comment: String,
    val createdAt: Instant = Instant.now(),
)
