package com.toiletgen.feature.auth.di

import com.toiletgen.feature.auth.viewmodel.AuthViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authFeatureModule = module {
    viewModel { AuthViewModel(get(), get()) }
}
