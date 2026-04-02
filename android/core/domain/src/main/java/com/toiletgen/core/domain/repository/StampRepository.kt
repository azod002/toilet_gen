package com.toiletgen.core.domain.repository

import com.toiletgen.core.common.Resource
import com.toiletgen.core.domain.model.StampTrade
import com.toiletgen.core.domain.model.UserStamp

interface StampRepository {
    suspend fun collectStamp(toiletId: String): Resource<UserStamp>
    suspend fun getMyStamps(): Resource<List<UserStamp>>
    suspend fun createTrade(receiverId: String, myStampId: String, theirStampId: String): Resource<StampTrade>
    suspend fun getMyTrades(): Resource<List<StampTrade>>
    suspend fun acceptTrade(tradeId: String): Resource<StampTrade>
    suspend fun declineTrade(tradeId: String): Resource<StampTrade>
}
