package com.toiletgen.core.domain.usecase

import com.toiletgen.core.domain.repository.ReportRepository

class GetYearlyReportUseCase(private val repository: ReportRepository) {
    suspend operator fun invoke(year: Int) = repository.getYearlyReport(year)
}
