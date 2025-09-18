package com.example.studyflash.ui.locations

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.studyflash.location.EnsureLocationPermissions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationsScreen(
    padding: PaddingValues,
    onEdit: (String) -> Unit,
    vm: LocationsViewModel = hiltViewModel()

) {
    val locations by vm.items.collectAsStateWithLifecycle()
    val currentId by vm.currentId.collectAsStateWithLifecycle()
    EnsureLocationPermissions(askBackground = true)

    val host = remember { SnackbarHostState() }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var showAdd by remember { mutableStateOf(false) }
    var toDeleteId by remember { mutableStateOf<String?>(null) }

    var afterPermission by remember { mutableStateOf<(() -> Unit)?>(null) }
    val permLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        val granted = res[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                res[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) afterPermission?.invoke()
    }

    Scaffold(
        modifier = Modifier.padding(padding),
        snackbarHost = { SnackbarHost(host) }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Locais Favoritos", style = MaterialTheme.typography.titleLarge)
            Text("Cadastre até 7 locais. Selecione o local atual e edite suas coordenadas/raio no mapa.")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        if (currentId != null) onEdit(currentId!!)
                    },
                    enabled = currentId != null,
                    modifier = Modifier.weight(1f)
                ) { Text("Editar atual no mapa") }

                Button(
                    onClick = {
                        if (currentId == null) return@Button

                        fun run() {
                            val fused = LocationServices.getFusedLocationProviderClient(ctx)
                            val fineGranted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            val coarseGranted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            if (!fineGranted && !coarseGranted) return

                            fused.lastLocation
                                .addOnSuccessListener { loc ->
                                    if (loc != null && currentId != null) {
                                        vm.updateCoordinates(currentId!!, loc.latitude, loc.longitude) { ok ->
                                            scope.launch {
                                                host.showSnackbar(
                                                    if (ok) "Coordenadas atualizadas" else "Falha ao atualizar coordenadas"
                                                )
                                            }
                                        }
                                    } else {
                                        scope.launch { host.showSnackbar("Não foi possível obter localização") }
                                    }
                                }
                                .addOnFailureListener {
                                    scope.launch { host.showSnackbar("Erro ao obter localização") }
                                }
                        }

                        val fineGranted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        val coarseGranted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        if (fineGranted || coarseGranted) run()
                        else {
                            afterPermission = { run() }
                            permLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    enabled = currentId != null,
                    modifier = Modifier.weight(1f)
                ) { Text("GPS → coordenadas do atual") }
            }

            Button(
                onClick = { showAdd = true },
                enabled = locations.size < 7,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Adicionar Local") }

            Divider()

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(locations, key = { it.id }) { loc ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row {
                            RadioButton(
                                selected = loc.id == currentId,
                                onClick = { vm.setCurrent(loc.id) }
                            )
                            Column {
                                Text(loc.name, style = MaterialTheme.typography.titleMedium)
                                val coords = if (loc.latitude != null && loc.longitude != null)
                                    "(${loc.latitude}, ${loc.longitude})" else "(sem coordenadas)"
                                Text(
                                    if (loc.id == currentId) "Atual • $coords • raio: ${loc.radiusMeters}m"
                                    else "$coords • raio: ${loc.radiusMeters}m",
                                    color = if (loc.id == currentId)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TextButton(onClick = { onEdit(loc.id) }) { Text("Editar no mapa") }
                            }
                        }
                        IconButton(onClick = { toDeleteId = loc.id }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                        }
                    }
                    Divider()
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }

    if (showAdd) AddLocationDialog(
        onDismiss = { showAdd = false },
        onConfirm = { name ->
            vm.add(name) { ok, msg ->
                showAdd = false
                scope.launch {
                    host.showSnackbar(if (ok) "Local adicionado" else (msg ?: "Falha ao adicionar local"))
                }
            }
        }
    )

    toDeleteId?.let { id ->
        ConfirmDeleteDialog(
            onDismiss = { toDeleteId = null },
            onConfirm = {
                vm.delete(id) {
                    toDeleteId = null
                }
            }
        )
    }
}

@Composable
private fun AddLocationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo local") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Dê um nome ao local (ex.: Biblioteca, Sala de Estudo, Quarto).")
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do local") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.trim()) }, enabled = name.isNotBlank()) {
                Text("Adicionar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun ConfirmDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir local") },
        text = { Text("Tem certeza que deseja excluir este local?") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Excluir") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
