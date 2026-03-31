package com.toiletgen.core.domain.repository

import com.toiletgen.core.common.Resource
import com.toiletgen.core.domain.model.AuthTokens
import com.toiletgen.core.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Resource<AuthTokens>
    suspend fun register(username: String, email: String, password: String): Resource<AuthTokens>
    suspend fun refreshToken(): Resource<AuthTokens>
    fun getCurrentUser(): Flow<Resource<User>>
    suspend fun logout()
}
