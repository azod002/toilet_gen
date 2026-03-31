package com.toiletgen.core.domain.model

data class YearlyReport(
    val year: Int,
    val toiletsVisited: Int,
    val reviewsWritten: Int,
    val sosSent: Int,
    val sosHelped: Int,
    val toiletsCreated: Int,
    val favoriteToilet: Toilet?,
    val topAchievements: List<Achievement>,
)
