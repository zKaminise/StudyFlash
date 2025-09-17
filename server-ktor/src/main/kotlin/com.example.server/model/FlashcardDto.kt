package com.example.server.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateFlashcardRequest(
    val type: String,
    val frontText: String? = null,
    val backText: String? = null,
    val wrong1: String? = null,
    val wrong2: String? = null,
    val wrong3: String? = null,
    val payloadJson: String? = null // ⬅️ novo
)

@Serializable
data class FlashcardDto(
    val id: Long? = null,
    val type: String,
    val frontText: String? = null,
    val backText: String? = null,
    val wrong1: String? = null,
    val wrong2: String? = null,
    val wrong3: String? = null,
    val payloadJson: String? = null // ⬅️ novo
)
