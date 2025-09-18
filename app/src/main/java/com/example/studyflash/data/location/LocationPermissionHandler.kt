package com.example.studyflash.location

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

/**
 * Gate de permissões. Coloque no topo da LocationsScreen.
 * - Pede FINE + COARSE
 * - Opcional: pede BACKGROUND (em fluxo separado) em Android 10+.
 */
@Composable
fun EnsureLocationPermissions(
    askBackground: Boolean,
    onAllGranted: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var showFineRationale by remember { mutableStateOf(false) }
    var showBackgroundInfo by remember { mutableStateOf(false) }

    val fineGranted = remember {
        derivedStateOf {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    val backgroundGranted = remember {
        derivedStateOf {
            // Só faz sentido checar se o SO suporta background separado (Android 10+)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) true else {
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    val fineLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) showFineRationale = true
    }

    val backgroundLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* resultado não mostra rationale automaticamente; guiamos pelo diálogo */ }

    // 1) Fine/Coarse primeiro
    LaunchedEffect(Unit) {
        if (!fineGranted.value) {
            fineLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // 2) Opcional: Background (Android 10+). Só pedimos se FINE já ok.
    LaunchedEffect(fineGranted.value, askBackground) {
        if (askBackground && fineGranted.value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Mostra uma folha informando por que pedir background
            showBackgroundInfo = !backgroundGranted.value
        }
    }

    // Quando tudo OK, avisa
    LaunchedEffect(fineGranted.value, backgroundGranted.value, askBackground) {
        if (fineGranted.value && (!askBackground || backgroundGranted.value)) {
            onAllGranted()
        }
    }

    // Rationale para FINE
    if (showFineRationale && activity != null) {
        PermissionRationaleDialog(
            title = "Permissão de Localização",
            message = "Precisamos da sua localização para associar seus estudos a locais favoritos.",
            confirm = "Abrir Configurações",
            dismiss = "Agora não",
            onConfirm = {
                showFineRationale = false
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", activity.packageName, null)
                )
                activity.startActivity(intent)
            },
            onDismiss = { showFineRationale = false }
        )
    }

    // Info/Fluxo para BACKGROUND (Android 10+)
    if (showBackgroundInfo && activity != null) {
        PermissionRationaleDialog(
            title = "Localização em 2º plano (opcional)",
            message = "Para detectar automaticamente quando você entra/sai de um local favorito (geofencing), " +
                    "o Android exige a permissão de localização em segundo plano.",
            confirm = "Permitir em 2º plano",
            dismiss = "Pedir depois",
            onConfirm = {
                showBackgroundInfo = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            },
            onDismiss = { showBackgroundInfo = false }
        )
    }
}

@Composable
private fun PermissionRationaleDialog(
    title: String,
    message: String,
    confirm: String,
    dismiss: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        text = {
            Column {
                Text(message)
                Spacer(Modifier.height(12.dp))
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) {
                Text(confirm)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(dismiss)
            }
        }
    )
}
