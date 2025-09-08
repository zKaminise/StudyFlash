package com.example.studyflash.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // FRONT_BACK | CLOZE | TYPING | MCQ
    val frontText: String? = null,
    val backText: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)