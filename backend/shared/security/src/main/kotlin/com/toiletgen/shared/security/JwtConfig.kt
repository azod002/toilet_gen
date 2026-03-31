package com.toiletgen.shared.security

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val accessTokenExpirationMs: Long = 3600_000, // 1 hour
    val refreshTokenExpirationMs: Long = 2_592_000_000, // 30 days
)
