package com.toiletgen.toilet.domain.model

import java.time.Instant
import java.util.UUID

data class Book(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val username: String,
    val title: String,
    val author: String,
    val fileSize: Long,
    val createdAt: Instant = Instant.now(),
)
