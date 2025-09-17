package com.example.studyflash.ui.locations

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLocationScreen(
    padding: PaddingValues,
    locationId: String,
    onDone: () -> Unit,
    vm: EditLocationViewModel = hiltViewModel()
) {
    val ctx = LocalContext.current
    val host = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val entity by vm.state.collectAsState(null)

    // Carrega dados do local
    LaunchedEffect(locationId) { vm.load(locationId) }

    // Estado inicial do mapa (fallback: Brasília)
    var latLng by remember { mutableStateOf(LatLng(-15.793889, -47.882778)) }
    var radius by remember { mutableStateOf(150f) }

    // Quando "entity" chega, ajusta posição/raio
    LaunchedEffect(entity) {
        entity?.let { e ->
            val lat = e.latitude ?: latLng.latitude
            val lon = e.longitude ?: latLng.longitude
            latLng = LatLng(lat, lon)
            radius = if (e.radiusMeters > 0f) e.radiusMeters else 150f
        }
    }

    // Pegar posição atual para centralizar (opcional)
    val requestPerms = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { /* permissões tratadas na próxima tentativa */ }

    fun centerOnMyLocation(cameraState: CameraPositionState) {
        val fineGranted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) {
            requestPerms.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }
        LocationServices.getFusedLocationProviderClient(ctx).lastLocation
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    latLng = LatLng(loc.latitude, loc.longitude)
                    scope.launch {
                        cameraState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(latLng, 17f),
                            durationMs = 600
                        )
                    }
                } else {
                    scope.launch { host.showSnackbar("Não foi possível obter sua localização") }
                }
            }
            .addOnFailureListener {
                scope.launch { host.showSnackbar("Erro ao obter localização") }
            }
    }

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(latLng, 16f)
    }

    Scaffold(snackbarHost = { SnackbarHost(host) }) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(padding)
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Editar local no mapa", style = MaterialTheme.typography.titleLarge)
            Text(entity?.name ?: "…")

            // Mapa: toque para reposicionar o marcador/círculo
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                cameraPositionState = cameraState,
                onMapClick = { p -> latLng = p }
            ) {
                Marker(state = MarkerState(position = latLng))
                Circle(
                    center = latLng,
                    radius = radius.toDouble(),
                    strokeWidth = 2f
                )
            }

            Button(
                onClick = { centerOnMyLocation(cameraState) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Centralizar na minha localização") }

            // Slider de raio (50m–300m)
            Text("Raio: ${radius.toInt()} m")
            Slider(
                value = radius,
                onValueChange = { radius = it },
                valueRange = 50f..300f,
                steps = 5,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors()
            )

            // Coordenadas (apenas leitura rápida)
            OutlinedTextField(
                value = TextFieldValue("${latLng.latitude}, ${latLng.longitude}"),
                onValueChange = {},
                label = { Text("Coordenadas selecionadas") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val okLat = latLng.latitude
                    val okLon = latLng.longitude
                    vm.save(locationId, okLat, okLon, radius) { ok ->
                        scope.launch {
                            if (ok) {
                                host.showSnackbar("Local salvo")
                                onDone()
                            } else {
                                host.showSnackbar("Falha ao salvar")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Salvar") }
        }
    }
}
