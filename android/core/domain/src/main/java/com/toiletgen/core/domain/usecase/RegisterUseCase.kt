package com.toiletgen.core.domain.usecase

import com.toiletgen.core.domain.repository.AuthRepository

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(username: String, email: String, password: String) =
        repository.register(username, email, password)
}
