package com.toiletgen.gamification.domain.model

import java.util.UUID

data class UserStats(
    val userId: UUID,
    val toiletsVisited: Int = 0,
    val reviewsWritten: Int = 0,
    val sosSent: Int = 0,
    val sosHelped: Int = 0,
    val toiletsCreated: Int = 0,
    val year: Int,
)
