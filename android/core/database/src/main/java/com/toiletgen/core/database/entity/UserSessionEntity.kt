package com.toiletgen.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_session")
data class UserSessionEntity(
    @PrimaryKey val id: Int = 1,
    val userId: String,
    val username: String,
    val email: String,
    val avatarUrl: String?,
    val role: String = "user",
    val accessToken: String,
    val refreshToken: String,
)
