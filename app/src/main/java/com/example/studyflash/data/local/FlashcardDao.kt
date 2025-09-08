package com.example.studyflash.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards")
    suspend fun listAll(): List<FlashcardEntity>

    @Query("SELECT * FROM flashcards WHERE id = :id")
    suspend fun getById(id: Long): FlashcardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FlashcardEntity): Long

    @Delete
    suspend fun delete(entity: FlashcardEntity)

    // Cartão “devido” (vencido) para estudo
    @Query("SELECT * FROM flashcards WHERE dueAt <= :now ORDER BY dueAt ASC LIMIT 1")
    suspend fun getNextDue(now: Long): FlashcardEntity?

    // Distratores para MCQ
    @Query("""
        SELECT backText FROM flashcards
        WHERE id != :id AND backText IS NOT NULL AND TRIM(backText) != ''
        ORDER BY RANDOM() LIMIT :n
    """)
    suspend fun getRandomBacks(id: Long, n: Int): List<String>

    @Query("SELECT COUNT(*) FROM flashcards")
    suspend fun countAll(): Int

    @Query("SELECT COUNT(*) FROM flashcards WHERE dueAt <= :now")
    suspend fun countDue(now: Long): Int

    @Query("DELETE FROM flashcards")
    suspend fun clearAll()

    @Query("""
        UPDATE flashcards
        SET easeFactor = :ease,
            intervalDays = :intervalDays,
            repetitions = :reps,
            dueAt = :dueAt,
            updatedAt = :updatedAt
        WHERE id = :id
    """)
    suspend fun updateSpaced(
        id: Long,
        ease: Double,
        intervalDays: Int,
        reps: Int,
        dueAt: Long,
        updatedAt: Long = System.currentTimeMillis()
    )
}
