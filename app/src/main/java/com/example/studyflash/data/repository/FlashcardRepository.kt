package com.example.studyflash.data.repository

import com.example.studyflash.data.local.AttemptHistoryDao
import com.example.studyflash.data.local.AttemptHistoryEntity
import com.example.studyflash.data.local.FlashcardDao
import com.example.studyflash.data.local.FlashcardEntity
import com.example.studyflash.domain.spaced.Sm2Engine
import com.example.studyflash.domain.spaced.Sm2State
import com.example.studyflash.domain.spaced.StudyGrade
import kotlinx.coroutines.flow.Flow

class FlashcardRepository(
    private val dao: FlashcardDao,
    private val attemptDao: AttemptHistoryDao
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

    suspend fun getNextDue(now: Long = System.currentTimeMillis()): FlashcardEntity? =
        dao.getNextDue(now)

    // Gera 4 opções (1 correta + até 3 distratores)
    suspend fun buildOptionsFor(card: FlashcardEntity, total: Int = 4): List<String> {
        val correct = (card.backText ?: "").ifBlank { "Sem resposta" }
        val needDistractors = (total - 1).coerceAtLeast(0)
        val others = dao.getRandomBacks(card.id, needDistractors)
            .map { it.ifBlank { "…" } }
            .toMutableList()

        // Completa com genéricos se faltar
        while (others.size < needDistractors) {
            others += listOf("Não sei", "Talvez", "Passar")[(others.size) % 3]
        }

        val all = (others + correct).distinct().toMutableList()
        all.shuffle()
        return all.take(total)
    }

    suspend fun recordReview(
        card: FlashcardEntity,
        grade: StudyGrade,
        timeToAnswerMs: Long
    ): FlashcardEntity {
        val now = System.currentTimeMillis()
        val quality = Sm2Engine.mapButtonToQuality(grade)

        val (newState, newDueAt) = Sm2Engine.review(
            Sm2State(card.easeFactor, card.intervalDays, card.repetitions),
            quality,
            now
        )

        attemptDao.insert(
            AttemptHistoryEntity(
                flashcardId = card.id,
                timestamp = now,
                grade = quality,
                timeToAnswerMs = timeToAnswerMs
            )
        )

        dao.updateSpaced(
            id = card.id,
            ease = newState.easeFactor,
            intervalDays = newState.intervalDays,
            reps = newState.repetitions,
            dueAt = newDueAt,
            updatedAt = now
        )

        return card.copy(
            easeFactor = newState.easeFactor,
            intervalDays = newState.intervalDays,
            repetitions = newState.repetitions,
            dueAt = newDueAt,
            updatedAt = now
        )
    }
}
