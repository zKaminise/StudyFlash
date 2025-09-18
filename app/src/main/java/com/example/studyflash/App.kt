package com.example.studyflash

import android.app.Application
import android.util.Log
import com.example.studyflash.data.repository.LocationsRepository
import com.example.studyflash.location.GeofencingManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltAndroidApp
class App : Application() {

    @Inject lateinit var locationsRepo: LocationsRepository
    @Inject lateinit var geofencingManager: GeofencingManager

    private val appScope = CoroutineScope(Dispatchers.Default + Job())

    override fun onCreate() {
        super.onCreate()

        // Observa a lista de locais e atualiza os geofences automaticamente
        appScope.launch {
            locationsRepo.observe().collectLatest { _ ->
                runCatching { geofencingManager.refreshAll() }
                    .onFailure { Log.w("App", "refresh geofences falhou: $it") }
            }
        }
    }
}
