package com.example.studyflash.data.repository

import com.example.studyflash.data.local.AttemptHistoryDao
import com.example.studyflash.data.local.AttemptHistoryEntity
import com.example.studyflash.data.local.FlashcardDao
import com.example.studyflash.data.local.FlashcardEntity
import com.example.studyflash.domain.spaced.StudyGrade
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.math.max

private const val AVOID_SAME_LOCATION_MS: Long = 15 * 60 * 1000 // 15 min

class FlashcardRepository @Inject constructor(
    private val dao: FlashcardDao,
    private val attemptDao: AttemptHistoryDao
) {

    fun observeLocal(): Flow<List<FlashcardEntity>> = dao.observeAll()

    suspend fun add(
        type: String,
        front: String?,
        back: String?,
        wrong1: String? = null,
        wrong2: String? = null,
        wrong3: String? = null
    ) {
        val now = System.currentTimeMillis()
        val entity = FlashcardEntity(
            /* id             = */ 0L,
            /* type           = */ type,
            /* frontText      = */ front,
            /* backText       = */ back,
            /* wrong1         = */ wrong1,
            /* wrong2         = */ wrong2,
            /* wrong3         = */ wrong3,
            /* createdAt      = */ now,
            /* updatedAt      = */ now,
            /* dueAt          = */ now,
            /* easeFactor     = */ 2.5,
            /* intervalDays   = */ 0,
            /* repetitions    = */ 0,
            /* lastLocationId = */ null,
            /* lastReviewedAt = */ null
        )
        dao.upsert(entity)
    }

    suspend fun delete(card: FlashcardEntity) {
        attemptDao.clearByCard(card.id)
        dao.delete(card)
    }

    // Próximo devido SEM considerar localização (fallback)
    suspend fun getNextDue(): FlashcardEntity? =
        dao.getNextDue(System.currentTimeMillis())

    // Próximo devido EVITANDO mesma localização nos últimos 15min (com fallback)
    suspend fun getNextDue(currentLocationId: String?): FlashcardEntity? {
        val now = System.currentTimeMillis()
        return if (currentLocationId.isNullOrBlank()) {
            dao.getNextDue(now)
        } else {
            dao.getNextDueAvoidingLocation(now, currentLocationId, AVOID_SAME_LOCATION_MS)
                ?: dao.getNextDue(now)
        }
    }

    suspend fun buildOptionsFor(card: FlashcardEntity, total: Int = 4): List<String> {
        val correct = card.backText?.takeIf { it.isNotBlank() } ?: "Sem resposta"

        val predefinedWrong = listOfNotNull(
            card.wrong1?.takeIf { it.isNotBlank() },
            card.wrong2?.takeIf { it.isNotBlank() },
            card.wrong3?.takeIf { it.isNotBlank() }
        )

        val base = if (predefinedWrong.isNotEmpty()) {
            (listOf(correct) + predefinedWrong).distinct()
        } else {
            val needDistractors = max(0, total - 1)
            val distractors = dao.getRandomBacks(card.id, needDistractors)
            (listOf(correct) + distractors).distinct()
        }

        val extras = generateSequence(1) { it + 1 }
            .map { "Opção $it" }
            .filter { it !in base }
            .take(max(0, total - base.size))
            .toList()

        return (base + extras).take(total).shuffled()
    }

    suspend fun recordAnswer(
        card: FlashcardEntity,
        correct: Boolean,
        currentLocationId: String? = null
    ) {
        val now = System.currentTimeMillis()
        val (intervalDays, ease, reps, nextDueAt) =
            if (correct) {
                val reps = card.repetitions + 1
                val ease = card.easeFactor
                val intervalDays = 1
                val due = now + 24L * 60 * 60 * 1000
                Quad(intervalDays, ease, reps, due)
            } else {
                val reps = 0
                val ease = card.easeFactor
                val intervalDays = 0
                val due = now + 2L * 60 * 1000
                Quad(intervalDays, ease, reps, due)
            }

        dao.updateSpaced(
            id = card.id,
            ease = ease,
            intervalDays = intervalDays,
            reps = reps,
            dueAt = nextDueAt,
            lastLocationId = currentLocationId,
            lastReviewedAt = now,
            updatedAt = now
        )

        attemptDao.insert(
            AttemptHistoryEntity(
                cardId = card.id,
                correct = correct,
                answeredAt = now,
                locationId = currentLocationId
            )
        )
    }

    suspend fun recordReview(
        card: FlashcardEntity,
        grade: StudyGrade,
        timeToAnswerMs: Long,
        currentLocationId: String? = null
    ) {
        val correct = when (grade) {
            StudyGrade.Again -> false
            StudyGrade.Hard, StudyGrade.Good, StudyGrade.Easy -> true
        }
        recordAnswer(card, correct, currentLocationId)
    }

    suspend fun countAll(): Int = dao.countAll()
    suspend fun countDue(nowMs: Long = System.currentTimeMillis()): Int = dao.countDue(nowMs)

    private data class Quad(
        val intervalDays: Int,
        val ease: Double,
        val reps: Int,
        val due: Long
    )
}
