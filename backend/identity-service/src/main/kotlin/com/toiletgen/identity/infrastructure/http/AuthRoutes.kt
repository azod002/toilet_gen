package com.toiletgen.identity.infrastructure.http

import com.toiletgen.identity.application.command.LoginCommand
import com.toiletgen.identity.application.command.RegisterCommand
import com.toiletgen.identity.application.command.UpdateProfileCommand
import com.toiletgen.identity.application.handler.AuthHandler
import com.toiletgen.identity.application.handler.ProfileHandler
import com.toiletgen.shared.security.userId
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class RegisterRequest(val username: String, val email: String, val password: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class TokenResponse(val accessToken: String, val refreshToken: String)

@Serializable
data class UserResponse(val id: String, val username: String, val email: String, val avatarUrl: String?, val role: String = "user")

@Serializable
data class UpdateProfileRequest(val username: String? = null, val avatarUrl: String? = null)

fun Route.authRoutes() {
    val authHandler by application.inject<AuthHandler>()
    val profileHandler by application.inject<ProfileHandler>()
    val userRepository by application.inject<com.toiletgen.identity.domain.repository.UserRepository>()

    route("/api/v1/auth") {
        post("/register") {
            val req = call.receive<RegisterRequest>()
            val tokens = authHandler.register(RegisterCommand(req.username, req.email, req.password))
            call.respond(HttpStatusCode.Created, TokenResponse(tokens.accessToken, tokens.refreshToken))
        }

        post("/login") {
            val req = call.receive<LoginRequest>()
            val tokens = authHandler.login(LoginCommand(req.email, req.password))
            call.respond(TokenResponse(tokens.accessToken, tokens.refreshToken))
        }
    }

    authenticate("auth-jwt") {
        route("/api/v1/users") {
            get("/me") {
                val user = profileHandler.getProfile(call.userId)
                call.respond(UserResponse(user.id.toString(), user.username, user.email, user.avatarUrl, user.role))
            }

            put("/me") {
                val req = call.receive<UpdateProfileRequest>()
                val user = profileHandler.updateProfile(
                    UpdateProfileCommand(call.userId, req.username, req.avatarUrl)
                )
                call.respond(UserResponse(user.id.toString(), user.username, user.email, user.avatarUrl, user.role))
            }

            get("/search") {
                val username = call.request.queryParameters["username"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "username parameter required"))
                val user = userRepository.findByUsername(username)
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Пользователь не найден"))
                call.respond(UserResponse(user.id.toString(), user.username, user.email, user.avatarUrl, user.role))
            }
        }
    }
}
