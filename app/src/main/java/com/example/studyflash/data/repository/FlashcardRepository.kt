package com.example.studyflash.data.repository

import com.example.studyflash.data.local.AttemptHistoryDao
import com.example.studyflash.data.local.AttemptHistoryEntity
import com.example.studyflash.data.local.FlashcardDao
import com.example.studyflash.data.local.FlashcardEntity
import com.example.studyflash.domain.spaced.StudyGrade
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.math.max

class FlashcardRepository @Inject constructor(
    private val dao: FlashcardDao,
    private val attemptDao: AttemptHistoryDao
) {

    fun observeLocal(): Flow<List<FlashcardEntity>> = dao.observeAll()

    /**
     * Cria um card local.
     * Para MCQ (múltipla escolha), preencha wrong1/2/3 com as alternativas erradas.
     */
    suspend fun add(
        type: String,
        front: String?,
        back: String?,
        wrong1: String? = null,
        wrong2: String? = null,
        wrong3: String? = null
    ) {
        val now = System.currentTimeMillis()
        dao.upsert(
            FlashcardEntity(
                type = type,
                frontText = front,
                backText = back,
                wrong1 = wrong1,
                wrong2 = wrong2,
                wrong3 = wrong3,
                createdAt = now,
                updatedAt = now,
                dueAt = now // disponível imediatamente para estudo
            )
        )
    }

    suspend fun delete(card: FlashcardEntity) {
        attemptDao.clearByCard(card.id)
        dao.delete(card)
    }

    suspend fun getNextDue(): FlashcardEntity? =
        dao.getNextDue(System.currentTimeMillis())

    /**
     * Monta opções de múltipla escolha para o card.
     * Preferência:
     * 1) Se o card tiver wrong1/2/3 preenchidos, usa [backText, wrong1, wrong2, wrong3].
     * 2) Caso contrário, busca distratores no banco para completar.
     */
    suspend fun buildOptionsFor(card: FlashcardEntity, total: Int = 4): List<String> {
        val correct = card.backText?.takeIf { it.isNotBlank() } ?: "Sem resposta"

        // Se o card tiver alternativas definidas, usa-as
        val predefinedWrong = listOfNotNull(
            card.wrong1?.takeIf { it.isNotBlank() },
            card.wrong2?.takeIf { it.isNotBlank() },
            card.wrong3?.takeIf { it.isNotBlank() }
        )

        val base = if (predefinedWrong.isNotEmpty()) {
            (listOf(correct) + predefinedWrong).distinct()
        } else {
            // Sem alternativas definidas => completa com distratores do banco
            val needDistractors = max(0, total - 1)
            val distractors = dao.getRandomBacks(card.id, needDistractors)
            (listOf(correct) + distractors).distinct()
        }

        // Garante que teremos exatamente 'total' opções (com placeholders se faltar)
        val extras = generateSequence(1) { it + 1 }
            .map { "Opção $it" }
            .filter { it !in base }
            .take(max(0, total - base.size))
            .toList()

        return (base + extras).take(total).shuffled()
    }

    /**
     * Registra resposta e agenda próxima revisão:
     * - errado => 2 minutos
     * - certo  => 24 horas
     */
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
                val intervalDays = 1                 // 24 horas
                val due = now + 24L * 60 * 60 * 1000
                Quad(intervalDays, ease, reps, due)
            } else {
                val reps = 0
                val ease = card.easeFactor
                val intervalDays = 0
                val due = now + 2L * 60 * 1000       // 2 minutos
                Quad(intervalDays, ease, reps, due)
            }

        dao.updateSpaced(
            id = card.id,
            ease = ease,
            intervalDays = intervalDays,
            reps = reps,
            dueAt = nextDueAt,
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

    /**
     * API usada pelo StudyViewModel: converte grade para correto/errado (2 min/24 h).
     */
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
        // timeToAnswerMs disponível para evoluir o algoritmo no futuro
    }

    private data class Quad(
        val intervalDays: Int,
        val ease: Double,
        val reps: Int,
        val due: Long
    )
}
