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
    val lastAnsweredAt: Long   // MAX(answeredAt)
)

@Dao
interface AttemptHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AttemptHistoryEntity)

    @Query("DELETE FROM attempt_history WHERE cardId = :cardId")
    suspend fun clearByCard(cardId: Long)

    // 🔽 NOVO: estatísticas por localização
    @Query(
        """
        SELECT 
            locationId AS locationId,
            COUNT(*) AS total,
            SUM(CASE WHEN correct THEN 1 ELSE 0 END) AS correctCount,
            MAX(answeredAt) AS lastAnsweredAt
        FROM attempt_history
        GROUP BY locationId
        ORDER BY lastAnsweredAt DESC
        """
    )
    suspend fun getStatsByLocation(): List<LocationStatsRow>
}
