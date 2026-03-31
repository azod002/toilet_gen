package com.toiletgen.identity.application.command

data class RegisterCommand(
    val username: String,
    val email: String,
    val password: String,
)

data class LoginCommand(
    val email: String,
    val password: String,
)

data class UpdateProfileCommand(
    val userId: String,
    val username: String?,
    val avatarUrl: String?,
)
