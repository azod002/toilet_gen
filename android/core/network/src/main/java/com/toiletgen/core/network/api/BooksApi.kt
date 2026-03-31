package com.toiletgen.core.network.api

import com.toiletgen.core.network.model.BookResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface BooksApi {
    @GET("api/v1/books")
    suspend fun getBooks(): List<BookResponse>

    @Multipart
    @POST("api/v1/books")
    suspend fun uploadBook(
        @Part("title") title: RequestBody,
        @Part("author") author: RequestBody,
        @Part file: MultipartBody.Part,
    ): BookResponse

    @DELETE("api/v1/books/{id}")
    suspend fun deleteBook(@Path("id") id: String)

    @GET("api/v1/books/{id}/download")
    @Streaming
    suspend fun downloadBook(@Path("id") id: String): ResponseBody
}
