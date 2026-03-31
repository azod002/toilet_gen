package com.toiletgen.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.toiletgen.core.database.dao.*
import com.toiletgen.core.database.entity.*

@Database(
    entities = [
        ToiletEntity::class,
        ReviewEntity::class,
        UserSessionEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class ToiletGenDatabase : RoomDatabase() {
    abstract fun toiletDao(): ToiletDao
    abstract fun reviewDao(): ReviewDao
    abstract fun userSessionDao(): UserSessionDao
}
