package com.toiletgen.core.domain.repository

import com.toiletgen.core.common.Resource
import com.toiletgen.core.domain.model.YearlyReport

interface ReportRepository {
    suspend fun getYearlyReport(year: Int): Resource<YearlyReport>
}
