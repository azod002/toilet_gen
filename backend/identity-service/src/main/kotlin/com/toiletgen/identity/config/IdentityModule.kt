package com.toiletgen.identity.config

import com.toiletgen.identity.application.handler.AuthHandler
import com.toiletgen.identity.application.handler.ProfileHandler
import com.toiletgen.identity.domain.event.IdentityEventPublisher
import com.toiletgen.identity.domain.repository.UserRepository
import com.toiletgen.identity.infrastructure.persistence.UserRepositoryImpl
import org.koin.dsl.module

val identityModule = module {
    single<UserRepository> { UserRepositoryImpl() }
    single { IdentityEventPublisher(get()) }
    single { AuthHandler(get(), get(), get()) }
    single { ProfileHandler(get(), get()) }
}
