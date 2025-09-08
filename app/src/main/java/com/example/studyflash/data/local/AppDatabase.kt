package com.example.studyflash.data.local


import androidx.room.Database
import androidx.room.RoomDatabase


@Database(
    entities = [FlashcardEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun flashcardDao(): FlashcardDao
}