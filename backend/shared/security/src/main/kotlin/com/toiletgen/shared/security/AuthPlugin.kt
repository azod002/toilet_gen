package com.toiletgen.shared.security

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureJwtAuth(jwtService: JwtService, config: JwtConfig) {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = config.realm
            verifier(jwtService.verifier)
            validate { credential ->
                if (credential.payload.getClaim("userId").asString().isNotEmpty()) {
                    JWTPrincipal(credential.payload)
                } else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Токен недействителен или истёк"))
            }
        }
    }
}

val ApplicationCall.userId: String
    get() = principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()

val ApplicationCall.username: String
    get() = principal<JWTPrincipal>()!!.payload.getClaim("username").asString()

val ApplicationCall.role: String
    get() = principal<JWTPrincipal>()!!.payload.getClaim("role").asString() ?: "user"
