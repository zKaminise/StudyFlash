package com.example.studyflash.data.remote

import com.example.studyflash.data.local.FlashcardEntity

fun FlashcardEntity.toCreateRequest(): CreateFlashcardRequest =
    CreateFlashcardRequest(
        type = type,
        frontText = frontText,
        backText = backText,
        wrong1 = wrong1,
        wrong2 = wrong2,
        wrong3 = wrong3
    )

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

fun FlashcardDto.toEntity(): FlashcardEntity {
    val now = System.currentTimeMillis()
    return FlashcardEntity(
        /* id             = */ id ?: 0L,
        /* type           = */ type,
        /* frontText      = */ frontText,
        /* backText       = */ backText,
        /* wrong1         = */ wrong1,
        /* wrong2         = */ wrong2,
        /* wrong3         = */ wrong3,
        /* createdAt      = */ createdAt ?: now,
        /* updatedAt      = */ updatedAt ?: now,
        /* dueAt          = */ dueAt ?: now,
        /* easeFactor     = */ easeFactor ?: 2.5,
        /* intervalDays   = */ intervalDays ?: 0,
        /* repetitions    = */ repetitions ?: 0,
        /* lastLocationId = */ null,
        /* lastReviewedAt = */ null
    )
}
