package com.example.studyflash.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,                 // FRONT_BACK | CLOZE | TYPING | MCQ
    val frontText: String? = null,
    val backText: String? = null,

    // Metadados
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Estado de repetição espaçada (SM-2)
    val easeFactor: Double = 2.5,     // EF inicial
    val intervalDays: Int = 0,        // intervalo atual em dias
    val repetitions: Int = 0,         // número de repetições consecutivas acertadas
    val dueAt: Long = System.currentTimeMillis() // quando o cartão “vence” de novo
)
