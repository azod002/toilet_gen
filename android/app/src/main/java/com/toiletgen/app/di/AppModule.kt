package com.toiletgen.app.di

import com.toiletgen.core.database.dao.UserSessionDao
import com.toiletgen.core.domain.repository.*
import com.toiletgen.core.domain.usecase.*
import com.toiletgen.core.network.TokenProvider
import com.toiletgen.app.data.repository.*
import com.toiletgen.app.data.TokenProviderImpl
import org.koin.dsl.module

val appModule = module {
    // Token Provider
    single<TokenProvider> { TokenProviderImpl(get()) }

    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }
    single<ToiletRepository> { ToiletRepositoryImpl(get(), get()) }
    single<ReviewRepository> { ReviewRepositoryImpl(get(), get()) }
    single<SosRepository> { SosRepositoryImpl(get()) }
    single<AchievementRepository> { AchievementRepositoryImpl(get()) }
    single<ReportRepository> { ReportRepositoryImpl(get()) }
    single<StampRepository> { StampRepositoryImpl(get()) }

    // Use Cases
    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
    factory { GetNearbyToiletsUseCase(get()) }
    factory { GetToiletDetailsUseCase(get()) }
    factory { CreateToiletUseCase(get()) }
    factory { AddReviewUseCase(get()) }
    factory { GetReviewsUseCase(get()) }
    factory { RequestSosUseCase(get()) }
    factory { GetAchievementsUseCase(get()) }
    factory { GetYearlyReportUseCase(get()) }
}
