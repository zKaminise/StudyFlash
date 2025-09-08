package com.example.studyflash.data.remote

import com.example.studyflash.data.local.FlashcardEntity

// Para ENVIAR ao servidor (criar/atualizar)
fun FlashcardEntity.toCreateRequest(): CreateFlashcardRequest =
    CreateFlashcardRequest(
        type = type,
        frontText = frontText,
        backText = backText,
        wrong1 = wrong1,
        wrong2 = wrong2,
        wrong3 = wrong3
    )

// Se você ainda usa em algum lugar um DTO local, pode manter:
fun FlashcardEntity.toDto(): FlashcardDto =
    FlashcardDto(
        id = if (id == 0L) null else id,
        type = type,
        frontText = frontText,
        backText = backText,
        wrong1 = wrong1,
        wrong2 = wrong2,
        wrong3 = wrong3,
        createdAt = createdAt,
        updatedAt = updatedAt,
        dueAt = dueAt,
        easeFactor = easeFactor,
        intervalDays = intervalDays,
        repetitions = repetitions
    )

// Para RECEBER do servidor
fun FlashcardDto.toEntity(): FlashcardEntity =
    FlashcardEntity(
        id = id ?: 0L,
        type = type,
        frontText = frontText,
        backText = backText,
        wrong1 = wrong1,
        wrong2 = wrong2,
        wrong3 = wrong3,
        createdAt = createdAt ?: System.currentTimeMillis(),
        updatedAt = updatedAt ?: System.currentTimeMillis(),
        dueAt = dueAt ?: System.currentTimeMillis(),
        easeFactor = easeFactor ?: 2.5,
        intervalDays = intervalDays ?: 0,
        repetitions = repetitions ?: 0
    )
