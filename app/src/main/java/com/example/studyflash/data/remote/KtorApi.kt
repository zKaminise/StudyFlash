package com.example.studyflash.data.remote

import retrofit2.http.*

data class FlashcardDto(
    val id: Long? = null,
    val type: String,
    val frontText: String?,
    val backText: String?
)

data class CreateFlashcardRequest(
    val type: String,
    val frontText: String?,
    val backText: String?
)

interface KtorApi {
    @GET("api/flashcards")
    suspend fun getAll(): List<FlashcardDto>

    @POST("api/flashcards")
    suspend fun create(@Body body: CreateFlashcardRequest): FlashcardDto

    @PUT("api/flashcards/{id}")
    suspend fun upsert(@Path("id") id: Long, @Body dto: FlashcardDto): FlashcardDto

    @DELETE("api/flashcards/{id}")
    suspend fun delete(@Path("id") id: Long)
}
