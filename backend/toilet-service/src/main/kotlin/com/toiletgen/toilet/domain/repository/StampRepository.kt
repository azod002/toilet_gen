package com.toiletgen.toilet.domain.repository

import com.toiletgen.toilet.domain.model.StampTrade
import com.toiletgen.toilet.domain.model.UserStamp
import java.time.Instant
import java.util.UUID

interface StampRepository {
    suspend fun collectStamp(userId: UUID, toiletId: UUID): UserStamp
    suspend fun getLastStampTime(userId: UUID, toiletId: UUID): Instant?
    suspend fun getUserStamps(userId: UUID): List<UserStamp>
    suspend fun getStampById(id: UUID): UserStamp?
    suspend fun deleteStamp(id: UUID)
    suspend fun transferStamp(stampId: UUID, newOwnerId: UUID)

    suspend fun createTrade(trade: StampTrade): StampTrade
    suspend fun getTradeById(id: UUID): StampTrade?
    suspend fun getPendingTradesForUser(userId: UUID): List<StampTrade>
    suspend fun updateTradeStatus(tradeId: UUID, status: String)
}
