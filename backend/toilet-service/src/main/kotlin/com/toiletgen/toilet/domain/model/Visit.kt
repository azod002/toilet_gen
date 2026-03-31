package com.toiletgen.toilet.domain.model

import java.time.Instant
import java.util.UUID

data class Visit(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val toiletId: UUID,
    val visitedAt: Instant = Instant.now(),
)
