package com.toiletgen.feature.entertainment.di

import com.toiletgen.feature.entertainment.viewmodel.BooksViewModel
import com.toiletgen.feature.entertainment.viewmodel.ForumViewModel
import com.toiletgen.feature.entertainment.viewmodel.JokesViewModel
import com.toiletgen.feature.entertainment.viewmodel.NewsViewModel
import com.toiletgen.feature.entertainment.viewmodel.RadioViewModel
import com.toiletgen.feature.entertainment.viewmodel.ThreadDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val entertainmentFeatureModule = module {
    viewModel { JokesViewModel() }
    viewModel { NewsViewModel() }
    viewModel { RadioViewModel() }
    viewModel { BooksViewModel(get(), get()) }
    viewModel { ForumViewModel(get()) }
    viewModel { params -> ThreadDetailViewModel(get(), params.get()) }
}
