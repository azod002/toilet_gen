package com.toiletgen.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "toilets")
data class ToiletEntity(
    @PrimaryKey val id: String,
    val ownerId: String?,
    val name: String,
    val description: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val isPaid: Boolean,
    val price: Double?,
    val hasToiletPaper: Boolean,
    val avgRating: Double,
    val avgCleanliness: Double,
    val reviewCount: Int,
    val createdAt: Long,
    val syncedAt: Long = System.currentTimeMillis(),
)
