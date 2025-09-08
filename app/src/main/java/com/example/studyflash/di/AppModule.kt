package com.example.studyflash.di


import android.app.Application
import androidx.room.Room
import com.example.studyflash.data.local.AppDatabase
import com.example.studyflash.data.remote.KtorApi
import com.example.studyflash.data.repository.FlashcardRepository
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    private const val BASE_URL = "http://10.0.2.2:8080/" // Emulador Android → host do PC


    @Provides @Singleton
    fun provideDb(app: Application): AppDatabase = Room.databaseBuilder(
        app, AppDatabase::class.java, "studyflash.db"
    ).fallbackToDestructiveMigration().build()


    @Provides @Singleton
    fun provideDao(db: AppDatabase) = db.flashcardDao()


    @Provides @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()


    @Provides @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().build()


    @Provides @Singleton
    fun provideRetrofit(ok: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(ok)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()


    @Provides @Singleton
    fun provideApi(retrofit: Retrofit): KtorApi = retrofit.create(KtorApi::class.java)


    @Provides @Singleton
    fun provideRepository(api: KtorApi, db: AppDatabase) = FlashcardRepository(api, db.flashcardDao())
}