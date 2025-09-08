package com.example.studyflash.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attempt_history")
data class AttemptHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val cardId: Long,
    val correct: Boolean,                    // true = acerto, false = erro
    val answeredAt: Long = System.currentTimeMillis(),
    val locationId: String? = null           // opcional: último local favoritado
)
