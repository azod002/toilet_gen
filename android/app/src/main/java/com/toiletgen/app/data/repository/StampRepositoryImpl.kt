package com.toiletgen.app.data.repository

import com.toiletgen.core.common.Resource
import com.toiletgen.core.domain.model.StampTrade
import com.toiletgen.core.domain.model.UserStamp
import com.toiletgen.core.domain.repository.StampRepository
import com.toiletgen.core.network.api.StampApi
import com.toiletgen.core.network.model.TradeRequest

class StampRepositoryImpl(
    private val stampApi: StampApi,
) : StampRepository {

    override suspend fun collectStamp(toiletId: String): Resource<UserStamp> = try {
        val resp = stampApi.collectStamp(toiletId)
        Resource.Success(resp.toDomain())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Ошибка получения марки")
    }

    override suspend fun getMyStamps(): Resource<List<UserStamp>> = try {
        val stamps = stampApi.getMyStamps().map { it.toDomain() }
        Resource.Success(stamps)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Ошибка загрузки марок")
    }

    override suspend fun createTrade(receiverId: String, myStampId: String, theirStampId: String): Resource<StampTrade> = try {
        val resp = stampApi.createTrade(TradeRequest(receiverId, myStampId, theirStampId))
        Resource.Success(resp.toDomain())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Ошибка создания обмена")
    }

    override suspend fun getMyTrades(): Resource<List<StampTrade>> = try {
        val trades = stampApi.getMyTrades().map { it.toDomain() }
        Resource.Success(trades)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Ошибка загрузки обменов")
    }

    override suspend fun acceptTrade(tradeId: String): Resource<StampTrade> = try {
        Resource.Success(stampApi.acceptTrade(tradeId).toDomain())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Ошибка принятия обмена")
    }

    override suspend fun declineTrade(tradeId: String): Resource<StampTrade> = try {
        Resource.Success(stampApi.declineTrade(tradeId).toDomain())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Ошибка отклонения обмена")
    }

    private fun com.toiletgen.core.network.model.StampResponse.toDomain() = UserStamp(
        id = id, toiletId = toiletId, toiletName = toiletName,
        toiletType = toiletType, obtainedAt = obtainedAt,
    )

    private fun com.toiletgen.core.network.model.TradeResponse.toDomain() = StampTrade(
        id = id, senderId = senderId, receiverId = receiverId,
        senderStampId = senderStampId, receiverStampId = receiverStampId,
        status = status, createdAt = createdAt,
    )
}
