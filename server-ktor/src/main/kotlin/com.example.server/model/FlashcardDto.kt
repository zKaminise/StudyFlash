package com.example.server.model


import kotlinx.serialization.Serializable


@Serializable
data class FlashcardDto(
    val id: Long? = null,
    val type: String,
    val frontText: String? = null,
    val backText: String? = null
)


@Serializable
data class CreateFlashcardRequest(
    val type: String,
    val frontText: String? = null,
    val backText: String? = null
)