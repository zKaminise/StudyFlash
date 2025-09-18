package com.example.studyflash.di

import android.content.Context
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides @Singleton
    fun provideGeofencingClient(
        @ApplicationContext context: Context
    ): GeofencingClient = LocationServices.getGeofencingClient(context)
}
