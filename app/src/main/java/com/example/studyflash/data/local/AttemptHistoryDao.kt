package com.example.studyflash.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AttemptHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AttemptHistoryEntity): Long

    @Query("SELECT * FROM attempt_history WHERE cardId = :cardId ORDER BY answeredAt DESC")
    suspend fun listByCard(cardId: Long): List<AttemptHistoryEntity>

    @Query("SELECT * FROM attempt_history ORDER BY answeredAt DESC")
    suspend fun listAll(): List<AttemptHistoryEntity> // ⬅️ novo (para Analytics)

    @Query("SELECT locationId FROM attempt_history WHERE cardId = :cardId ORDER BY answeredAt DESC LIMIT 1")
    suspend fun lastLocation(cardId: Long): String?

    @Query("""
        SELECT answeredAt FROM attempt_history
        WHERE cardId = :cardId AND locationId = :locationId
        ORDER BY answeredAt DESC LIMIT 1
    """)
    suspend fun lastAnsweredAtAtLocation(cardId: Long, locationId: String): Long?

    @Query("DELETE FROM attempt_history WHERE cardId = :cardId")
    suspend fun clearByCard(cardId: Long)

    @Query("DELETE FROM attempt_history")
    suspend fun clearAll()
}
