package com.toiletgen.core.database.dao

import androidx.room.*
import com.toiletgen.core.database.entity.UserSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSessionDao {
    @Query("SELECT * FROM user_session WHERE id = 1")
    fun getSession(): Flow<UserSessionEntity?>

    @Query("SELECT * FROM user_session WHERE id = 1")
    suspend fun getSessionSync(): UserSessionEntity?

    @Upsert
    suspend fun upsert(session: UserSessionEntity)

    @Query("DELETE FROM user_session")
    suspend fun clear()
}
