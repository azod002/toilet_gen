package com.toiletgen.app.data.repository

import com.toiletgen.core.common.Resource
import com.toiletgen.core.database.dao.UserSessionDao
import com.toiletgen.core.database.entity.UserSessionEntity
import com.toiletgen.core.domain.model.AuthTokens
import com.toiletgen.core.domain.model.User
import com.toiletgen.core.domain.repository.AuthRepository
import com.toiletgen.core.network.api.AuthApi
import com.toiletgen.core.network.model.LoginRequest
import com.toiletgen.core.network.model.RefreshTokenRequest
import com.toiletgen.core.network.model.RegisterRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val userSessionDao: UserSessionDao,
    private val tokenProvider: com.toiletgen.core.network.TokenProvider,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Resource<AuthTokens> {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            tokenProvider.saveTokens(response.accessToken, response.refreshToken)
            val userResponse = authApi.getCurrentUser()
            userSessionDao.upsert(
                UserSessionEntity(
                    userId = userResponse.id, username = userResponse.username,
                    email = userResponse.email, avatarUrl = userResponse.avatarUrl,
                    role = userResponse.role, accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                )
            )
            Resource.Success(AuthTokens(response.accessToken, response.refreshToken))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Ошибка входа")
        }
    }

    override suspend fun register(username: String, email: String, password: String): Resource<AuthTokens> {
        return try {
            val response = authApi.register(RegisterRequest(username, email, password))
            tokenProvider.saveTokens(response.accessToken, response.refreshToken)
            val userResponse = authApi.getCurrentUser()
            userSessionDao.upsert(
                UserSessionEntity(
                    userId = userResponse.id, username = userResponse.username,
                    email = userResponse.email, avatarUrl = userResponse.avatarUrl,
                    role = userResponse.role, accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                )
            )
            Resource.Success(AuthTokens(response.accessToken, response.refreshToken))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Ошибка регистрации")
        }
    }

    override suspend fun refreshToken(): Resource<AuthTokens> {
        return try {
            val refreshToken = tokenProvider.getRefreshToken() ?: return Resource.Error("Нет refresh token")
            val response = authApi.refreshToken(RefreshTokenRequest(refreshToken))
            tokenProvider.saveTokens(response.accessToken, response.refreshToken)
            Resource.Success(AuthTokens(response.accessToken, response.refreshToken))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Ошибка обновления токена")
        }
    }

    override fun getCurrentUser(): Flow<Resource<User>> {
        return userSessionDao.getSession().map { session ->
            if (session != null) {
                Resource.Success(User(session.userId, session.username, session.email, session.avatarUrl, session.role))
            } else {
                Resource.Error("Пользователь не авторизован")
            }
        }
    }

    override suspend fun logout() {
        userSessionDao.clear()
    }
}
