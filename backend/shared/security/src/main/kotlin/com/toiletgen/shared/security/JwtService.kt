package com.toiletgen.shared.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import java.util.Date

class JwtService(private val config: JwtConfig) {

    private val algorithm = Algorithm.HMAC256(config.secret)

    val verifier = JWT.require(algorithm)
        .withIssuer(config.issuer)
        .withAudience(config.audience)
        .build()

    fun generateAccessToken(userId: String, username: String, role: String = "user"): String =
        JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withClaim("userId", userId)
            .withClaim("username", username)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + config.accessTokenExpirationMs))
            .sign(algorithm)

    fun generateRefreshToken(userId: String): String =
        JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withClaim("userId", userId)
            .withClaim("type", "refresh")
            .withExpiresAt(Date(System.currentTimeMillis() + config.refreshTokenExpirationMs))
            .sign(algorithm)

    fun extractUserId(token: DecodedJWT): String =
        token.getClaim("userId").asString()

    fun extractUsername(token: DecodedJWT): String =
        token.getClaim("username").asString()
}
