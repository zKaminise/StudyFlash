package com.example.studyflash.di

import android.content.Context
import androidx.room.Room
import com.example.studyflash.data.local.AppDatabase
import com.example.studyflash.data.local.AttemptHistoryDao
import com.example.studyflash.data.local.FlashcardDao
import com.example.studyflash.data.remote.KtorApi
import com.example.studyflash.data.repository.FlashcardRepository
import com.example.studyflash.data.repository.LocationsRepository
import com.example.studyflash.data.sync.SyncManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- DB ---
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "studyflash.db")
            .fallbackToDestructiveMigration()
            .build()

    // --- DAOs ---
    @Provides @Singleton
    fun provideFlashcardDao(db: AppDatabase): FlashcardDao = db.flashcardDao()

    @Provides @Singleton
    fun provideAttemptHistoryDao(db: AppDatabase): AttemptHistoryDao = db.attemptHistoryDao()

    // --- OkHttp / Retrofit ---
    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/") // ajuste para seu IP ao gerar APK
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

    @Provides @Singleton
    fun provideKtorApi(retrofit: Retrofit): KtorApi =
        retrofit.create(KtorApi::class.java)

    // --- Repositórios / Sync ---
    @Provides @Singleton
    fun provideFlashcardRepository(
        flashcardDao: FlashcardDao,
        attemptHistoryDao: AttemptHistoryDao
    ): FlashcardRepository = FlashcardRepository(flashcardDao, attemptHistoryDao)

    @Provides @Singleton
    fun provideLocationsRepository(db: AppDatabase): LocationsRepository =
        LocationsRepository(db.favoriteLocationDao())

    @Provides @Singleton
    fun provideSyncManager(
        api: KtorApi,
        flashcardDao: FlashcardDao,
        @ApplicationContext context: Context
    ): SyncManager = SyncManager(api, flashcardDao, context)
}
