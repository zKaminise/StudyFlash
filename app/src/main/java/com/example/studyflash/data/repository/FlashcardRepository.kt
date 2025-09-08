package com.example.studyflash.data.repository

import com.example.studyflash.data.local.FlashcardDao
import com.example.studyflash.data.local.FlashcardEntity
import kotlinx.coroutines.flow.Flow

class FlashcardRepository(
    private val dao: FlashcardDao
) {
    fun observeLocal(): Flow<List<FlashcardEntity>> = dao.observeAll()

    suspend fun add(type: String, front: String?, back: String?) {
        dao.upsert(
            FlashcardEntity(
                type = type,
                frontText = front?.ifBlank { null },
                backText = back?.ifBlank { null }
            )
        )
    }

    suspend fun delete(card: FlashcardEntity) {
        dao.delete(card)
    }
}
