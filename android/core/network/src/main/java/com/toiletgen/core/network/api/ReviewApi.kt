package com.toiletgen.core.network.api

import com.toiletgen.core.network.model.*
import retrofit2.http.*

interface ReviewApi {
    @GET("api/v1/toilets/{toiletId}/reviews")
    suspend fun getReviews(@Path("toiletId") toiletId: String): List<ReviewResponse>

    @POST("api/v1/toilets/{toiletId}/reviews")
    suspend fun addReview(
        @Path("toiletId") toiletId: String,
        @Body request: CreateReviewRequest,
    ): ReviewResponse
}
