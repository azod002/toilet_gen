package com.toiletgen.feature.toilet_details.di

import com.toiletgen.feature.toilet_details.viewmodel.AddReviewViewModel
import com.toiletgen.feature.toilet_details.viewmodel.ToiletDetailsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val toiletDetailsFeatureModule = module {
    viewModel { ToiletDetailsViewModel(get(), get(), get(), get(), get()) }
    viewModel { AddReviewViewModel(get()) }
}
