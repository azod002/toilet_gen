package com.toiletgen.app.data.repository

import com.toiletgen.core.common.Resource
import com.toiletgen.core.domain.model.YearlyReport
import com.toiletgen.core.domain.repository.ReportRepository
import com.toiletgen.core.network.api.ReportApi

class ReportRepositoryImpl(
    private val reportApi: ReportApi,
) : ReportRepository {

    override suspend fun getYearlyReport(year: Int): Resource<YearlyReport> {
        return try {
            val response = reportApi.getYearlyReport(year)
            Resource.Success(YearlyReport(
                year = response.year, toiletsVisited = response.toiletsVisited,
                reviewsWritten = response.reviewsWritten, sosSent = response.sosSent,
                sosHelped = response.sosHelped, toiletsCreated = response.toiletsCreated,
                favoriteToilet = null, topAchievements = emptyList(),
            ))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Ошибка загрузки отчёта")
        }
    }
}
