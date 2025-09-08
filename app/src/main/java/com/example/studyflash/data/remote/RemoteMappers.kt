package com.example.studyflash.data.remote

import com.example.studyflash.data.local.FlashcardEntity

fun FlashcardEntity.toDto(): FlashcardDto =
    FlashcardDto(
        id = if (id == 0L) null else id,
        type = type,
        frontText = frontText,
        backText = backText
    )

fun FlashcardDto.toEntity(): FlashcardEntity =
    FlashcardEntity(
        id = id ?: 0L,
        type = type,
        frontText = frontText,
        backText = backText,
        // mantém os default dos campos SM-2 conforme seu entity
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
