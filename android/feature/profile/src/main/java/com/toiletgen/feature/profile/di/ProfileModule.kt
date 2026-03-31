package com.toiletgen.feature.profile.di

import com.toiletgen.feature.profile.viewmodel.ProfileViewModel
import com.toiletgen.feature.profile.viewmodel.VisitHistoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val profileFeatureModule = module {
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { VisitHistoryViewModel(get()) }
}
