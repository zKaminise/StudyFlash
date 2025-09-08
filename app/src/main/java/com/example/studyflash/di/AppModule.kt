package com.example.studyflash.di

import android.content.Context
import androidx.room.Room
import com.example.studyflash.data.local.AppDatabase
import com.example.studyflash.data.local.AttemptHistoryDao
import com.example.studyflash.data.local.FlashcardDao
import com.example.studyflash.data.remote.KtorApi
import com.example.studyflash.data.repository.FlashcardRepository
import com.example.studyflash.data.sync.SyncManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val DB_NAME = "studyflash.db"

    // 👇 AQUI você define a URL base do servidor:
    // Emulador Android -> host da máquina é 10.0.2.2
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // Se for testar em CELULAR FÍSICO na mesma rede do PC,
    // troque por algo como: "http://192.168.0.10:8080/"
    // private const val BASE_URL = "http://SEU_IP_LOCAL:8080/"

    @Provides @Singleton
    fun provideDb(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, DB_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton
    fun provideFlashcardDao(db: AppDatabase): FlashcardDao = db.flashcardDao()

    @Provides @Singleton
    fun provideAttemptHistoryDao(db: AppDatabase): AttemptHistoryDao = db.attemptHistoryDao()

    @Provides @Singleton
    fun provideOkHttp(): OkHttpClient {
        val logging = okhttp3.logging.HttpLoggingInterceptor().apply {
            level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL) // 👈 usando a BASE_URL aqui
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides @Singleton
    fun provideKtorApi(retrofit: Retrofit): KtorApi =
        retrofit.create(KtorApi::class.java)

    @Provides @Singleton
    fun provideRepository(
        dao: FlashcardDao,
        attemptDao: AttemptHistoryDao
    ): FlashcardRepository = FlashcardRepository(dao, attemptDao)

    @Provides @Singleton
    fun provideSyncManager(
        api: KtorApi,
        dao: FlashcardDao
    ): SyncManager = SyncManager(api, dao)
}
