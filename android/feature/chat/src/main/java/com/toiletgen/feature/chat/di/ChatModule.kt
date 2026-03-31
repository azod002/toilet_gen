package com.toiletgen.feature.chat.di

import com.toiletgen.feature.chat.viewmodel.ConversationsViewModel
import com.toiletgen.feature.chat.viewmodel.GlobalChatViewModel
import com.toiletgen.feature.chat.viewmodel.PrivateChatViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val chatFeatureModule = module {
    viewModel { GlobalChatViewModel(get()) }
    viewModel { ConversationsViewModel(get()) }
    viewModel { params -> PrivateChatViewModel(get(), params.get(), params.get()) }
}
