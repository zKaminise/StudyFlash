package com.example.studyflash.data.location

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.studyflash.data.local.FavoriteLocationEntity
import com.example.studyflash.receiver.GeofenceReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceManager @Inject constructor(
    private val context: Context,
    private val geofencingClient: GeofencingClient
) {
    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    private fun pendingIntent(): PendingIntent {
        val intent = Intent(context, GeofenceReceiver::class.java)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        return PendingIntent.getBroadcast(context, 0, intent, flags)
    }

    fun refreshGeofences(favorites: List<FavoriteLocationEntity>) {
        if (!hasLocationPermission()) return

        geofencingClient.removeGeofences(pendingIntent()).addOnCompleteListener {
            val geofences = favorites.mapNotNull { loc ->
                val lat = loc.latitude ?: return@mapNotNull null
                val lon = loc.longitude ?: return@mapNotNull null
                val radius = (loc.radiusMeters.takeIf { it > 0 } ?: 150f)
                Geofence.Builder()
                    .setRequestId(loc.id)
                    .setCircularRegion(lat, lon, radius)
                    .setTransitionTypes(
                        Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL
                    )
                    .setLoiteringDelay(2 * 60 * 1000) // 2 min parado
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .build()
            }

            if (geofences.isEmpty()) return@addOnCompleteListener

            val request = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofences)
                .build()

            try {
                geofencingClient.addGeofences(request, pendingIntent())
            } catch (_: SecurityException) {
                // sem permissão — ignore
            }
        }
    }
}
