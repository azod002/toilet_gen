package com.toiletgen.feature.yearly_report.di

import com.toiletgen.feature.yearly_report.viewmodel.YearlyReportViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val yearlyReportFeatureModule = module {
    viewModel { YearlyReportViewModel(get()) }
}
