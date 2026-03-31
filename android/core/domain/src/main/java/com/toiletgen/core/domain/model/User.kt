package com.toiletgen.core.domain.model

data class User(
    val id: String,
    val username: String,
    val email: String,
    val avatarUrl: String?,
    val role: String = "user",
)

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
)
