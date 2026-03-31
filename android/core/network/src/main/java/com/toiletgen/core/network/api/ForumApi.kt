package com.toiletgen.core.network.api

import com.toiletgen.core.network.model.*
import retrofit2.http.*

interface ForumApi {
    @GET("api/v1/forum/threads")
    suspend fun getThreads(@Query("page") page: Int = 0): List<ThreadResponse>

    @GET("api/v1/forum/threads/{id}")
    suspend fun getThread(@Path("id") id: String): ThreadDetailResponse

    @POST("api/v1/forum/threads")
    suspend fun createThread(@Body request: CreateThreadRequest): ThreadResponse

    @POST("api/v1/forum/threads/{id}/replies")
    suspend fun createReply(@Path("id") threadId: String, @Body request: CreateReplyRequest): ReplyResponse

    @DELETE("api/v1/forum/threads/{id}")
    suspend fun deleteThread(@Path("id") id: String)
}
