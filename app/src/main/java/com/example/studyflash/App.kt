package com.example.studyflash

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.studyflash.data.repository.LocationsRepository
import com.example.studyflash.location.GeofencingManager

@HiltAndroidApp
class App : Application() {

    @Inject lateinit var locationsRepo: LocationsRepository
    @Inject lateinit var geofencing: GeofencingManager

    override fun onCreate() {
        super.onCreate()

        // (Opcional) Tenta reconstruir geofences ao iniciar o app.
        // Se a permissão não existir, o método retorna sem fazer nada.
        CoroutineScope(Dispatchers.Default).launch {
            runCatching {
                val all = locationsRepo.listAll()
                geofencing.rebuildGeofences(all) { /* ignore result no boot */ }
            }
        }
    }
}
