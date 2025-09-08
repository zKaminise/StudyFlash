package com.example.studyflash.domain.spaced

import kotlin.math.roundToInt

data class Sm2State(
    val easeFactor: Double,
    val intervalDays: Int,
    val repetitions: Int
)

enum class StudyGrade { Again, Hard, Good, Easy }

/**
 * Engine ajustado para intervalos FIXOS:
 * - Errada (Again) -> 2 minutos
 * - Correta (Hard/Good/Easy) -> 24 horas
 *
 * Observação:
 * - Mantemos os campos de estado (EF, reps, intervalDays) para compatibilidade,
 *   mas eles não influenciam no cálculo (apenas registramos algo coerente).
 */
object Sm2Engine {

    private const val WRONG_RETRY_DELAY_MS = 2 * 60_000L       // 2 minutos
    private const val RIGHT_RETRY_DELAY_MS = 24 * 60 * 60_000L // 24 horas (86_400_000 ms)
    private const val MIN_EF = 1.3

    /**
     * quality: 0..5 (nosso mapeamento: Again=1, Hard=3, Good=4, Easy=5)
     * Retorna (novo estado, dueAt em ms).
     */
    fun review(
        state: Sm2State,
        quality: Int,
        nowMs: Long
    ): Pair<Sm2State, Long> {

        // Mantemos EF no mínimo 1.3 e não variamos EF/intervalo incremental
        val ef = state.easeFactor.coerceAtLeast(MIN_EF)

        return if (quality < 3) {
            // ❌ Resposta errada → reexibir em 2 minutos
            val newState = Sm2State(
                easeFactor = ef,
                intervalDays = 0,
                repetitions = 0
            )
            newState to (nowMs + WRONG_RETRY_DELAY_MS)
        } else {
            // ✅ Resposta correta → reexibir em 24 horas (sem progressão de SM-2)
            val newState = Sm2State(
                easeFactor = ef,
                intervalDays = 1, // simbólico, não é usado para cálculo incremental
                repetitions = (state.repetitions + 1).coerceAtLeast(1)
            )
            newState to (nowMs + RIGHT_RETRY_DELAY_MS)
        }
    }

    fun mapButtonToQuality(button: StudyGrade): Int = when (button) {
        StudyGrade.Again -> 1
        StudyGrade.Hard  -> 3
        StudyGrade.Good  -> 4
        StudyGrade.Easy  -> 5
    }
}