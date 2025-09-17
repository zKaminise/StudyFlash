package com.example.studyflash.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.studyflash.data.repository.LocationsRepository
import com.google.android.gms.location.GeofencingEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GeofenceReceiver : BroadcastReceiver() {

    @Inject lateinit var locationsRepo: LocationsRepository

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) {
            Log.w("GeofenceReceiver", "geofencing error: ${event.errorCode}")
            return
        }

        val transition = event.geofenceTransition
        val ids = event.triggeringGeofences?.mapNotNull { it.requestId } ?: emptyList()

        // Quando entrar ou "dwell", marcamos o primeiro como atual
        if (ids.isNotEmpty() &&
            (transition == com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER ||
                    transition == com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_DWELL)
        ) {
            val targetId = ids.first()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    locationsRepo.setCurrent(targetId)
                } catch (e: Exception) {
                    Log.e("GeofenceReceiver", "setCurrent failed", e)
                }
            }
        }
    }
}
