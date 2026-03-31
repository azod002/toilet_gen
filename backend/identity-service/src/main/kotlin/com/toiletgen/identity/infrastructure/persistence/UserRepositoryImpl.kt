package com.toiletgen.identity.infrastructure.persistence

import com.toiletgen.identity.domain.model.User
import com.toiletgen.identity.domain.repository.UserRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class UserRepositoryImpl : UserRepository {

    private fun ResultRow.toUser() = User(
        id = this[UsersTable.id],
        username = this[UsersTable.username],
        email = this[UsersTable.email],
        passwordHash = this[UsersTable.passwordHash],
        avatarUrl = this[UsersTable.avatarUrl],
        role = this[UsersTable.role],
        createdAt = this[UsersTable.createdAt],
    )

    override suspend fun findById(id: UUID): User? = newSuspendedTransaction {
        UsersTable.selectAll().where { UsersTable.id eq id }.singleOrNull()?.toUser()
    }

    override suspend fun findByEmail(email: String): User? = newSuspendedTransaction {
        UsersTable.selectAll().where { UsersTable.email eq email }.singleOrNull()?.toUser()
    }

    override suspend fun findByUsername(username: String): User? = newSuspendedTransaction {
        UsersTable.selectAll().where { UsersTable.username eq username }.singleOrNull()?.toUser()
    }

    override suspend fun create(user: User): User = newSuspendedTransaction {
        UsersTable.insert {
            it[id] = user.id
            it[username] = user.username
            it[email] = user.email
            it[passwordHash] = user.passwordHash
            it[avatarUrl] = user.avatarUrl
            it[role] = user.role
            it[createdAt] = user.createdAt
        }
        user
    }

    override suspend fun update(user: User): User = newSuspendedTransaction {
        UsersTable.update({ UsersTable.id eq user.id }) {
            it[username] = user.username
            it[avatarUrl] = user.avatarUrl
        }
        user
    }
}
