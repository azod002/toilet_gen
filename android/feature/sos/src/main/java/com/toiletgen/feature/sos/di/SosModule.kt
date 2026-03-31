package com.toiletgen.feature.sos.di

import com.toiletgen.feature.sos.viewmodel.SosViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val sosFeatureModule = module {
    viewModel { SosViewModel(get()) }
}
