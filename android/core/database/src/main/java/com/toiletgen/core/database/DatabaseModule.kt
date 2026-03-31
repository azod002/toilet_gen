package com.toiletgen.core.database

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            ToiletGenDatabase::class.java,
            "toiletgen.db"
        ).fallbackToDestructiveMigration().build()
    }

    single { get<ToiletGenDatabase>().toiletDao() }
    single { get<ToiletGenDatabase>().reviewDao() }
    single { get<ToiletGenDatabase>().userSessionDao() }
}
