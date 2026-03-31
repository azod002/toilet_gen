package com.toiletgen.sos.config

import com.toiletgen.sos.application.handler.SosHandler
import com.toiletgen.sos.domain.event.SosEventPublisher
import com.toiletgen.sos.domain.repository.SosRepository
import com.toiletgen.sos.infrastructure.persistence.SosRepositoryImpl
import org.koin.dsl.module

val sosModule = module {
    single<SosRepository> { SosRepositoryImpl() }
    single { SosEventPublisher(get()) }
    single { SosHandler(get(), get()) }
}
