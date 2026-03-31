package com.toiletgen.app.data

import com.toiletgen.core.database.dao.UserSessionDao
import com.toiletgen.core.database.entity.UserSessionEntity
import com.toiletgen.core.network.TokenProvider
import kotlinx.coroutines.runBlocking

class TokenProviderImpl(
    private val userSessionDao: UserSessionDao,
) : TokenProvider {

    override fun getAccessToken(): String? = runBlocking {
        userSessionDao.getSessionSync()?.accessToken
    }

    override fun getRefreshToken(): String? = runBlocking {
        userSessionDao.getSessionSync()?.refreshToken
    }

    override fun saveTokens(accessToken: String, refreshToken: String) {
        runBlocking {
            val existing = userSessionDao.getSessionSync()
            val updated = existing?.copy(accessToken = accessToken, refreshToken = refreshToken)
                ?: UserSessionEntity(
                    userId = "", username = "", email = "",
                    avatarUrl = null, accessToken = accessToken, refreshToken = refreshToken,
                )
            userSessionDao.upsert(updated)
        }
    }

    override fun clearTokens() {
        runBlocking { userSessionDao.clear() }
    }
}
