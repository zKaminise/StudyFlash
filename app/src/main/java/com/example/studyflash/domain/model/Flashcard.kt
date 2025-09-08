package com.example.server.model

data class Flashcard(
    val id: Long,
    val type: String,
    val frontText: String?,
    val backText: String?,
    val wrong1: String?,
    val wrong2: String?,
    val wrong3: String?
)
