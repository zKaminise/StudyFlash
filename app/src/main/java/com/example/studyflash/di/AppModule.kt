package com.example.studyflash.di

import android.content.Context
import androidx.room.Room
import com.example.studyflash.data.local.AppDatabase
import com.example.studyflash.data.local.AttemptHistoryDao
import com.example.studyflash.data.local.FlashcardDao
import com.example.studyflash.data.repository.FlashcardRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val DB_NAME = "studyflash.db"

    @Provides @Singleton
    fun provideDb(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, DB_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton
    fun provideFlashcardDao(db: AppDatabase): FlashcardDao = db.flashcardDao()

    @Provides @Singleton
    fun provideAttemptDao(db: AppDatabase): AttemptHistoryDao = db.attemptHistoryDao()

    @Provides @Singleton
    fun provideRepository(
        flashcardDao: FlashcardDao,
        attemptDao: AttemptHistoryDao
    ): FlashcardRepository =
        FlashcardRepository(flashcardDao, attemptDao)
}
