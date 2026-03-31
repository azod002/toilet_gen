package com.toiletgen.core.network.api

import com.toiletgen.core.network.model.AchievementResponse
import retrofit2.http.GET

interface AchievementApi {
    @GET("api/v1/achievements")
    suspend fun getAllAchievements(): List<AchievementResponse>

    @GET("api/v1/achievements/me")
    suspend fun getMyAchievements(): List<AchievementResponse>
}
