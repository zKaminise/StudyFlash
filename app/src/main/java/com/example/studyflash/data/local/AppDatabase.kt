package com.example.studyflash.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        FlashcardEntity::class,
        AttemptHistoryEntity::class,
        FavoriteLocationEntity::class
    ],
    version = 8, // ⬅️ bump por causa do campo radiusMeters
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun flashcardDao(): FlashcardDao
    abstract fun attemptHistoryDao(): AttemptHistoryDao
    abstract fun favoriteLocationDao(): FavoriteLocationDao
}
