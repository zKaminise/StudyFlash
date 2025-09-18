package com.example.studyflash.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Linha de resultado para estatísticas agrupadas por localização.
 * locationId pode ser null (sem local definido).
 */
data class LocationStatsRow(
    val locationId: String?,   // null => "Sem local"
    val total: Int,            // COUNT(*)
    val correctCount: Int,     // SUM(correct ? 1 : 0)
    val avgTimeMs: Long?,      // AVG(timeToAnswerMs)
    val lastAnsweredAt: Long?  // MAX(answeredAt)
)

@Dao
interface AttemptHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AttemptHistoryEntity)

    @Query("DELETE FROM attempt_history WHERE cardId = :cardId")
    suspend fun clearByCard(cardId: Long)

    // Para heatmap / filtros por período (7/30/90 dias)
    @Query(
        """
        SELECT * FROM attempt_history
        WHERE answeredAt BETWEEN :from AND :to
        ORDER BY answeredAt ASC
        """
    )
    suspend fun listBetween(from: Long, to: Long): List<AttemptHistoryEntity>

    // Estatísticas por localização no intervalo
    @Query(
        """
        SELECT 
            locationId AS locationId,
            COUNT(*) AS total,
            SUM(CASE WHEN correct THEN 1 ELSE 0 END) AS correctCount,
            AVG(timeToAnswerMs) AS avgTimeMs,
            MAX(answeredAt) AS lastAnsweredAt
        FROM attempt_history
        WHERE answeredAt BETWEEN :from AND :to
        GROUP BY locationId
        ORDER BY total DESC
        """
    )
    suspend fun getStatsByLocation(from: Long, to: Long): List<LocationStatsRow>
}
