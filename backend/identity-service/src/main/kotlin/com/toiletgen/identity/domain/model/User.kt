package com.toiletgen.identity.domain.model

import java.time.Instant
import java.util.UUID

data class User(
    val id: UUID = UUID.randomUUID(),
    val username: String,
    val email: String,
    val passwordHash: String,
    val avatarUrl: String? = null,
    val role: String = "user",
    val createdAt: Instant = Instant.now(),
)
