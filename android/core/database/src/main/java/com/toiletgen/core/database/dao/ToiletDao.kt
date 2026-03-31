package com.toiletgen.core.database.dao

import androidx.room.*
import com.toiletgen.core.database.entity.ToiletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ToiletDao {
    @Query("SELECT * FROM toilets ORDER BY avgRating DESC")
    fun getAll(): Flow<List<ToiletEntity>>

    @Query("SELECT * FROM toilets WHERE id = :id")
    fun getById(id: String): Flow<ToiletEntity?>

    @Query("""
        SELECT * FROM toilets
        WHERE latitude BETWEEN :minLat AND :maxLat
        AND longitude BETWEEN :minLon AND :maxLon
        ORDER BY avgRating DESC
    """)
    fun getNearby(minLat: Double, maxLat: Double, minLon: Double, maxLon: Double): Flow<List<ToiletEntity>>

    @Upsert
    suspend fun upsertAll(toilets: List<ToiletEntity>)

    @Upsert
    suspend fun upsert(toilet: ToiletEntity)

    @Query("DELETE FROM toilets WHERE syncedAt < :threshold")
    suspend fun deleteStale(threshold: Long)

    @Query("DELETE FROM toilets WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("""
        DELETE FROM toilets
        WHERE latitude BETWEEN :minLat AND :maxLat
        AND longitude BETWEEN :minLon AND :maxLon
    """)
    suspend fun deleteNearby(minLat: Double, maxLat: Double, minLon: Double, maxLon: Double)
}
