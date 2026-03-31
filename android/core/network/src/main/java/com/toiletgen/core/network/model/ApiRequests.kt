package com.toiletgen.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RegisterRequest(val username: String, val email: String, val password: String)

@Serializable
data class RefreshTokenRequest(val refreshToken: String)

@Serializable
data class CreateToiletRequest(
    val name: String,
    val description: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val isPaid: Boolean,
    val price: Double? = null,
    val hasToiletPaper: Boolean,
)

@Serializable
data class CreateReviewRequest(
    val rating: Int,
    val cleanlinessSmell: Int,
    val cleanlinessDirt: Int,
    val hasToiletPaper: Boolean,
    val comment: String,
)

@Serializable
data class CreateSosRequest(val latitude: Double, val longitude: Double)

