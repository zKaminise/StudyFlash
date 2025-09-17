package com.example.studyflash.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    // tipo: "mcq" será o padrão
    val type: String = "mcq",

    // conteúdo
    val frontText: String? = null,   // pergunta
    val backText: String? = null,    // resposta correta

    // alternativas erradas (para MCQ)
    val wrong1: String? = null,
    val wrong2: String? = null,
    val wrong3: String? = null,

    // controle temporal / SRS
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val dueAt: Long = System.currentTimeMillis(),
    val easeFactor: Double = 2.5,
    val intervalDays: Int = 0,
    val repetitions: Int = 0,

    // (opcional) local/tempo da última revisão
    val lastLocationId: String? = null,
    val lastReviewedAt: Long? = null
)
