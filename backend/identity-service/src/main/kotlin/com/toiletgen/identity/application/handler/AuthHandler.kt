package com.toiletgen.identity.application.handler

import at.favre.lib.crypto.bcrypt.BCrypt
import com.toiletgen.identity.application.command.LoginCommand
import com.toiletgen.identity.application.command.RegisterCommand
import com.toiletgen.identity.domain.event.IdentityEventPublisher
import com.toiletgen.identity.domain.model.User
import com.toiletgen.identity.domain.repository.UserRepository
import com.toiletgen.shared.security.JwtService

data class TokenPair(val accessToken: String, val refreshToken: String)

class AuthHandler(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val eventPublisher: IdentityEventPublisher,
) {
    suspend fun register(command: RegisterCommand): TokenPair {
        require(userRepository.findByEmail(command.email) == null) { "Email уже занят" }
        require(userRepository.findByUsername(command.username) == null) { "Имя пользователя занято" }

        val hash = BCrypt.withDefaults().hashToString(12, command.password.toCharArray())
        val user = User(username = command.username, email = command.email, passwordHash = hash)
        val saved = userRepository.create(user)

        eventPublisher.userRegistered(saved.id.toString(), saved.username, saved.email)

        return TokenPair(
            accessToken = jwtService.generateAccessToken(saved.id.toString(), saved.username, saved.role),
            refreshToken = jwtService.generateRefreshToken(saved.id.toString()),
        )
    }

    suspend fun login(command: LoginCommand): TokenPair {
        val user = userRepository.findByEmail(command.email)
            ?: throw IllegalArgumentException("Неверный email или пароль")

        val result = BCrypt.verifyer().verify(command.password.toCharArray(), user.passwordHash)
        require(result.verified) { "Неверный email или пароль" }

        return TokenPair(
            accessToken = jwtService.generateAccessToken(user.id.toString(), user.username, user.role),
            refreshToken = jwtService.generateRefreshToken(user.id.toString()),
        )
    }
}
