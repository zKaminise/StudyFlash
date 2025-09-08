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

    @Query("DELETE FROM attempt_history WHERE cardId = :cardId")
    suspend fun clearByCard(cardId: Long)

    @Query("DELETE FROM attempt_history")
    suspend fun clearAll()
}
