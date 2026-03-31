package com.toiletgen.toilet.config

import com.toiletgen.toilet.application.handler.ToiletHandler
import com.toiletgen.toilet.domain.event.ToiletEventPublisher
import com.toiletgen.toilet.domain.repository.ReviewRepository
import com.toiletgen.toilet.domain.repository.ToiletRepository
import com.toiletgen.toilet.domain.repository.BookRepository
import com.toiletgen.toilet.domain.repository.VisitRepository
import com.toiletgen.toilet.infrastructure.persistence.BookRepositoryImpl
import com.toiletgen.toilet.infrastructure.persistence.ReviewRepositoryImpl
import com.toiletgen.toilet.infrastructure.persistence.ToiletRepositoryImpl
import com.toiletgen.toilet.infrastructure.persistence.VisitRepositoryImpl
import org.koin.dsl.module

val toiletModule = module {
    single<ToiletRepository> { ToiletRepositoryImpl() }
    single<ReviewRepository> { ReviewRepositoryImpl() }
    single<VisitRepository> { VisitRepositoryImpl() }
    single<BookRepository> { BookRepositoryImpl() }
    single { ToiletEventPublisher(get()) }
    single { ToiletHandler(get(), get(), get(), get()) }
}
