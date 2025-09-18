package com.example.studyflash.location

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.studyflash.receiver.GeofenceReceiver
import com.example.studyflash.data.repository.LocationsRepository
import com.example.studyflash.data.local.FavoriteLocationEntity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class GeofencingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationsRepo: LocationsRepository
) {
    private val client by lazy { LocationServices.getGeofencingClient(context) }

    private val pendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceReceiver::class.java)
        val flags = when {
            Build.VERSION.SDK_INT >= 31 -> PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            else -> PendingIntent.FLAG_UPDATE_CURRENT
        }
        PendingIntent.getBroadcast(context, 0, intent, flags)
    }

    /**
     * Remove todos os geofences e registra novamente com base nos locais favoritos.
     * Chame isso quando:
     *  - o app iniciar
     *  - a lista de locais mudar (add/delete/update/raio)
     */
    suspend fun refreshAll() {
        val items = locationsRepo.snapshot()
        val geofences = items
            .mapNotNull { it.toGeofenceOrNull() }

        // Remove existentes
        runCatching { client.removeGeofences(pendingIntent).awaitTask() }
            .onFailure { Log.w("GeofencingManager", "removeGeofences falhou: $it") }

        if (geofences.isEmpty()) return

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER) // dispara enter se já estiver dentro
            .addGeofences(geofences)
            .build()

        // Adiciona novos
        runCatching { client.addGeofences(request, pendingIntent).awaitTask() }
            .onSuccess { Log.d("GeofencingManager", "Geofences registrados: ${geofences.size}") }
            .onFailure { Log.e("GeofencingManager", "addGeofences falhou", it) }
    }

    private fun FavoriteLocationEntity.toGeofenceOrNull(): Geofence? {
        val lat = latitude ?: return null
        val lon = longitude ?: return null
        val radius = max(50f, radiusMeters ?: 150f) // mínimo 50m
        return Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(lat, lon, radius)
            .setLoiteringDelay(2 * 60 * 1000) // 2 min p/ DWELL
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL
            )
            .build()
    }
}

/** Helpers para transformar Task em suspensão sem coroutines-play-services extra */
private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitTask(): T {
    return kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        addOnSuccessListener { res -> if (cont.isActive) cont.resume(res, onCancellation = null) }
        addOnFailureListener { err -> if (cont.isActive) cont.resumeWith(Result.failure(err)) }
    }
}
