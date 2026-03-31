package com.toiletgen.core.network.api

import com.toiletgen.core.network.model.CreateReportRequest
import com.toiletgen.core.network.model.ReportResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ReportsApi {
    @POST("api/v1/reports")
    suspend fun createReport(@Body request: CreateReportRequest): ReportResponse
}
