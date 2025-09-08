package com.example.studyflash.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// DTO que o servidor retorna (pode ter mais/menos campos; deixamos defaults para não quebrar)
data class FlashcardDto(
    val id: Long? = null,
    val type: String,
    val frontText: String? = null,
    val backText: String? = null,
    val wrong1: String? = null,
    val wrong2: String? = null,
    val wrong3: String? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val dueAt: Long? = null,
    val easeFactor: Double? = null,
    val intervalDays: Int? = null,
    val repetitions: Int? = null
)

// Body usado para criar/atualizar (não leva id)
data class CreateFlashcardRequest(
    val type: String,
    val frontText: String?,
    val backText: String?,
    val wrong1: String?,
    val wrong2: String?,
    val wrong3: String?
)

interface KtorApi {

    @GET("api/flashcards")
    suspend fun getAll(): List<FlashcardDto>

    @POST("api/flashcards")
    suspend fun create(@Body body: CreateFlashcardRequest): FlashcardDto

    // 👇 endpoint para atualizar/upsert um card já existente
    @PUT("api/flashcards/{id}")
    suspend fun upsert(
        @Path("id") id: Long,
        @Body body: CreateFlashcardRequest
    ): FlashcardDto

    @DELETE("api/flashcards/{id}")
    suspend fun delete(@Path("id") id: Long)
}
