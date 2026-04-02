package com.toiletgen.feature.stamps.di

import com.toiletgen.feature.stamps.viewmodel.StampsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val stampsModule = module {
    viewModel { StampsViewModel(get()) }
}
