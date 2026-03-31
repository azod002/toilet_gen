package com.toiletgen.core.network.api

import com.toiletgen.core.network.model.ChatMessageResponse
import com.toiletgen.core.network.model.ConversationResponse
import com.toiletgen.core.network.model.PrivateMessageResponse
import com.toiletgen.core.network.model.SendMessageRequest
import com.toiletgen.core.network.model.UserResponse
import retrofit2.http.*

interface ChatApi {
    @GET("api/v1/users/search")
    suspend fun searchUser(@Query("username") username: String): UserResponse

    @GET("api/v1/chat/messages")
    suspend fun getGlobalMessages(@Query("since") since: Long? = null): List<ChatMessageResponse>

    @POST("api/v1/chat/messages")
    suspend fun sendGlobalMessage(@Body request: SendMessageRequest): ChatMessageResponse

    @GET("api/v1/chat/conversations")
    suspend fun getConversations(): List<ConversationResponse>

    @GET("api/v1/chat/private/{userId}")
    suspend fun getPrivateMessages(
        @Path("userId") userId: String,
        @Query("since") since: Long? = null,
    ): List<PrivateMessageResponse>

    @POST("api/v1/chat/private/{userId}")
    suspend fun sendPrivateMessage(
        @Path("userId") userId: String,
        @Query("receiverUsername") receiverUsername: String,
        @Body request: SendMessageRequest,
    ): PrivateMessageResponse
}
