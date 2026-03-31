package com.toiletgen.identity.infrastructure.persistence

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object UsersTable : Table("users") {
    val id = uuid("id")
    val username = varchar("username", 100).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val avatarUrl = varchar("avatar_url", 500).nullable()
    val role = varchar("role", 20).default("user")
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}
