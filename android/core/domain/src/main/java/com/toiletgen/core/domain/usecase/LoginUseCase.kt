package com.toiletgen.core.domain.usecase

import com.toiletgen.core.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String) =
        repository.login(email, password)
}
