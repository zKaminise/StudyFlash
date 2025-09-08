package com.example.studyflash.data.remote


import retrofit2.http.*


// DTOs espelham o servidor Ktor


data class FlashcardDto(
    val id: Long?,
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


    @DELETE("api/flashcards/{id}")
    suspend fun delete(@Path("id") id: Long)
}