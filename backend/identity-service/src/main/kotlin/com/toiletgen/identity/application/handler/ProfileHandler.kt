package com.toiletgen.identity.application.handler

import com.toiletgen.identity.application.command.UpdateProfileCommand
import com.toiletgen.identity.domain.event.IdentityEventPublisher
import com.toiletgen.identity.domain.model.User
import com.toiletgen.identity.domain.repository.UserRepository
import java.util.UUID

class ProfileHandler(
    private val userRepository: UserRepository,
    private val eventPublisher: IdentityEventPublisher,
) {
    suspend fun getProfile(userId: String): User {
        return userRepository.findById(UUID.fromString(userId))
            ?: throw IllegalArgumentException("Пользователь не найден")
    }

    suspend fun updateProfile(command: UpdateProfileCommand): User {
        val user = userRepository.findById(UUID.fromString(command.userId))
            ?: throw IllegalArgumentException("Пользователь не найден")

        val updated = user.copy(
            username = command.username ?: user.username,
            avatarUrl = command.avatarUrl ?: user.avatarUrl,
        )
        val saved = userRepository.update(updated)

        eventPublisher.userProfileUpdated(saved.id.toString(), saved.username, saved.avatarUrl)

        return saved
    }
}
