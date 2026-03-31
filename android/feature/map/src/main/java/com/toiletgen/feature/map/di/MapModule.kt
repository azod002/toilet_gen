package com.toiletgen.feature.map.di

import com.toiletgen.feature.map.viewmodel.AddToiletViewModel
import com.toiletgen.feature.map.viewmodel.MapViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mapFeatureModule = module {
    viewModel { MapViewModel(get(), get()) }
    viewModel { AddToiletViewModel(get()) }
}
