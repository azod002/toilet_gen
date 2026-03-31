package com.toiletgen.core.network.api

import com.toiletgen.core.network.model.YearlyReportResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ReportApi {
    @GET("api/v1/report/yearly/{year}")
    suspend fun getYearlyReport(@Path("year") year: Int): YearlyReportResponse
}
