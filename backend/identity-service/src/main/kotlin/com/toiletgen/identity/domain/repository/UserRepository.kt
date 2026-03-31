package com.toiletgen.identity.domain.repository

import com.toiletgen.identity.domain.model.User
import java.util.UUID

interface UserRepository {
    suspend fun findById(id: UUID): User?
    suspend fun findByEmail(email: String): User?
    suspend fun findByUsername(username: String): User?
    suspend fun create(user: User): User
    suspend fun update(user: User): User
}
