package com.toiletgen.core.network.api

import com.toiletgen.core.network.model.*
import retrofit2.http.*

interface ToiletApi {
    @GET("api/v1/toilets")
    suspend fun getNearbyToilets(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("radius") radius: Double,
    ): List<ToiletResponse>

    @GET("api/v1/toilets/{id}")
    suspend fun getToiletById(@Path("id") id: String): ToiletResponse

    @POST("api/v1/toilets")
    suspend fun createToilet(@Body request: CreateToiletRequest): ToiletResponse

    @PUT("api/v1/toilets/{id}")
    suspend fun updateToilet(@Path("id") id: String, @Body request: CreateToiletRequest): ToiletResponse

    @DELETE("api/v1/toilets/{id}")
    suspend fun deleteToilet(@Path("id") id: String)

    @POST("api/v1/toilets/{id}/visit")
    suspend fun visitToilet(@Path("id") id: String)

    @GET("api/v1/toilets/visits/me")
    suspend fun getMyVisits(): List<VisitResponse>
}
