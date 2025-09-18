package com.example.studyflash

import android.app.Application
import com.example.studyflash.data.repository.LocationsRepository
import com.example.studyflash.location.GeofencingManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class App : Application() {

    @Inject lateinit var locationsRepo: LocationsRepository
    @Inject lateinit var geofencing: GeofencingManager

    override fun onCreate() {
        super.onCreate()

        // Reconstrói geofences no boot, se permissões existirem.
        CoroutineScope(Dispatchers.Default).launch {
            runCatching {
                val all = locationsRepo.listAll()
                geofencing.rebuildGeofences(all) { /* ignore */ }
            }
        }
    }
}
