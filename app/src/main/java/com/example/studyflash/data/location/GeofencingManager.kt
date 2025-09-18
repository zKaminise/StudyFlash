package com.example.studyflash.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.studyflash.data.local.FavoriteLocationEntity
import com.example.studyflash.receiver.GeofenceReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofencingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geofencingClient: GeofencingClient
) {

    private val pendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceReceiver::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        PendingIntent.getBroadcast(context, 0, intent, flags)
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val bgOk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        return fine && bgOk
    }

    /**
     * Reconstrói TODAS as geofences com base na lista de favoritos salva.
     * Remove as anteriores e adiciona as novas válidas (lat/lon != null).
     */
    @SuppressLint("MissingPermission") // já verificamos em hasLocationPermission()
    fun rebuildGeofences(favorites: List<FavoriteLocationEntity>, onDone: (Boolean) -> Unit = {}) {
        if (!hasLocationPermission()) {
            onDone(false); return
        }

        geofencingClient.removeGeofences(pendingIntent).addOnCompleteListener {
            val geofences = favorites.mapNotNull { fav ->
                val lat = fav.latitude
                val lon = fav.longitude
                val radius = fav.radiusMeters ?: 150f
                if (lat == null || lon == null) return@mapNotNull null

                Geofence.Builder()
                    .setRequestId(fav.id)
                    .setCircularRegion(lat, lon, radius)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(
                        Geofence.GEOFENCE_TRANSITION_ENTER or
                                Geofence.GEOFENCE_TRANSITION_DWELL
                    )
                    .setLoiteringDelay(3 * 60 * 1000) // dwell ~3 min
                    .build()
            }

            if (geofences.isEmpty()) {
                onDone(true); return@addOnCompleteListener
            }

            val request = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofences)
                .build()

            try {
                geofencingClient.addGeofences(request, pendingIntent)
                    .addOnSuccessListener { onDone(true) }
                    .addOnFailureListener { onDone(false) }
            } catch (_: SecurityException) {
                onDone(false)
            }
        }
    }
}
