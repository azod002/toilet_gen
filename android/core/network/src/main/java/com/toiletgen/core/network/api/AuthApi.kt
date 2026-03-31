package com.toiletgen.core.network.api

import com.toiletgen.core.network.model.*
import retrofit2.http.*

interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): TokenResponse

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): TokenResponse

    @GET("api/v1/users/me")
    suspend fun getCurrentUser(): UserResponse
}
