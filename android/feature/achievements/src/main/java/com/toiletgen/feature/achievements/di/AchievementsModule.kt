package com.toiletgen.feature.achievements.di

import com.toiletgen.feature.achievements.viewmodel.AchievementsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val achievementsFeatureModule = module {
    viewModel { AchievementsViewModel(get()) }
}
