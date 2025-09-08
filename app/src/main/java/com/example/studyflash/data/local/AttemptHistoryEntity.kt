package com.example.studyflash.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attempt_history")
data class AttemptHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val flashcardId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val grade: Int,                  // 0-5 (SM-2). Aqui usamos 1,3,4,5 (Again/Hard/Good/Easy)
    val timeToAnswerMs: Long = 0L
)
