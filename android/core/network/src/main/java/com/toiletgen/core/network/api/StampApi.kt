package com.toiletgen.core.network.api

import com.toiletgen.core.network.model.StampResponse
import com.toiletgen.core.network.model.TradeRequest
import com.toiletgen.core.network.model.TradeResponse
import retrofit2.http.*

interface StampApi {
    @POST("api/v1/toilets/{id}/collect-stamp")
    suspend fun collectStamp(@Path("id") toiletId: String): StampResponse

    @GET("api/v1/stamps/me")
    suspend fun getMyStamps(): List<StampResponse>

    @POST("api/v1/stamps/trade")
    suspend fun createTrade(@Body request: TradeRequest): TradeResponse

    @GET("api/v1/stamps/trades/me")
    suspend fun getMyTrades(): List<TradeResponse>

    @POST("api/v1/stamps/trade/{id}/accept")
    suspend fun acceptTrade(@Path("id") tradeId: String): TradeResponse

    @POST("api/v1/stamps/trade/{id}/decline")
    suspend fun declineTrade(@Path("id") tradeId: String): TradeResponse
}
