package com.toiletgen.core.network.api

import com.toiletgen.core.network.model.*
import retrofit2.http.*

interface SosApi {
    @POST("api/v1/sos/request")
    suspend fun createSosRequest(@Body request: CreateSosRequest): SosRequestResponse

    @GET("api/v1/sos/request/{id}/status")
    suspend fun getSosStatus(@Path("id") id: String): SosRequestResponse

    @POST("api/v1/sos/request/{id}/accept")
    suspend fun acceptSos(@Path("id") id: String)

    @POST("api/v1/sos/request/{id}/decline")
    suspend fun declineSos(@Path("id") id: String)
}
