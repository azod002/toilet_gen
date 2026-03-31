package com.toiletgen.core.database.dao

import androidx.room.*
import com.toiletgen.core.database.entity.ReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE toiletId = :toiletId ORDER BY createdAt DESC")
    fun getByToiletId(toiletId: String): Flow<List<ReviewEntity>>

    @Upsert
    suspend fun upsertAll(reviews: List<ReviewEntity>)

    @Upsert
    suspend fun upsert(review: ReviewEntity)
}
