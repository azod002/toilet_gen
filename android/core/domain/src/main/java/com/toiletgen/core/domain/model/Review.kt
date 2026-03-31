package com.toiletgen.core.domain.model

data class Review(
    val id: String,
    val toiletId: String,
    val userId: String,
    val username: String,
    val rating: Int,
    val cleanlinessSmell: Int,
    val cleanlinessDirt: Int,
    val hasToiletPaper: Boolean,
    val comment: String,
    val createdAt: Long,
)
