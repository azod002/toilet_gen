package com.toiletgen.core.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.toiletgen.core.network.api.*
import com.toiletgen.core.network.interceptor.AuthInterceptor
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

fun networkModule(apiBaseUrl: String) = module {
    single {
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }

    single {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(get()))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single {
        val json: Json = get()
        Retrofit.Builder()
            .baseUrl(apiBaseUrl)
            .client(get())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    single { get<Retrofit>().create(AuthApi::class.java) }
    single { get<Retrofit>().create(ToiletApi::class.java) }
    single { get<Retrofit>().create(ReviewApi::class.java) }
    single { get<Retrofit>().create(SosApi::class.java) }
    single { get<Retrofit>().create(AchievementApi::class.java) }
    single { get<Retrofit>().create(ReportApi::class.java) }
    single { get<Retrofit>().create(BooksApi::class.java) }
    single { get<Retrofit>().create(ChatApi::class.java) }
    single { get<Retrofit>().create(ForumApi::class.java) }
    single { get<Retrofit>().create(ReportsApi::class.java) }
    single { get<Retrofit>().create(StampApi::class.java) }
}
