package com.example.studyflash.di

import android.content.Context
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient
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
    fun provideFusedLocationProviderClient(
        @ApplicationContext ctx: Context
    ): FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(ctx)

    @Provides @Singleton
    fun provideGeofencingClient(
        @ApplicationContext ctx: Context
    ): GeofencingClient =
        LocationServices.getGeofencingClient(ctx)
}
