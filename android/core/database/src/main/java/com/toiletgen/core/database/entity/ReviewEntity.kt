package com.toiletgen.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val toiletId: String,
    val userId: String,
    val username: String,
    val rating: Int,
    val cleanlinessSmell: Int,
    val cleanlinessDirt: Int,
    val hasToiletPaper: Boolean,
    val comment: String,
    val createdAt: Long,
    val syncedAt: Long = System.currentTimeMillis(),
)
