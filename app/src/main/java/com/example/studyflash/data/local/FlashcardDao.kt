package com.example.studyflash.data.local


import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<FlashcardEntity>>


    @Query("SELECT * FROM flashcards WHERE id = :id")
    suspend fun getById(id: Long): FlashcardEntity?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FlashcardEntity): Long


    @Delete
    suspend fun delete(entity: FlashcardEntity)
}