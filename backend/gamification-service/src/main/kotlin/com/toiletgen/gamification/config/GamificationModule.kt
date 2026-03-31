package com.toiletgen.gamification.config

import com.toiletgen.gamification.application.handler.EventHandler
import com.toiletgen.gamification.application.handler.GamificationHandler
import com.toiletgen.gamification.domain.event.GamificationEventPublisher
import com.toiletgen.gamification.domain.model.AchievementCatalog
import com.toiletgen.gamification.domain.repository.GamificationRepository
import com.toiletgen.gamification.infrastructure.persistence.GamificationRepositoryImpl
import org.koin.dsl.module

val gamificationModule = module {
    single<GamificationRepository> { GamificationRepositoryImpl() }
    single { GamificationEventPublisher(get()) }
    single { GamificationHandler(get(), get()) }
    single { EventHandler(get()) }
}
