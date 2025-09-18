package com.example.studyflash.ui.permissions

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*

/**
 * Usa assim dentro de uma Composable:
 *
 * val (askFine, askBg) = rememberLocationPermissionLaunchers()
 * Button(onClick = { askFine() }) { Text("Permitir localização precisa") }
 * Button(onClick = { askBg(activity)) { Text("Permitir localização em segundo plano") }
 */
@Composable
fun rememberLocationPermissionLaunchers(): Pair<() -> Unit, (Activity) -> Unit> {
    val fineLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* ignore; você pode guardar num state se quiser */ }
    )
    val bgLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { /* retorna das configurações */ }
    )

    val askFine = {
        fineLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val askBackground: (Activity) -> Unit = { activity ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // BACKGROUND não pode ser pedido com o mesmo diálogo; guia o usuário até as configurações
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", activity.packageName, null)
            )
            bgLauncher.launch(intent)
        }
        // Em < Q não existe BACKGROUND separado
    }

    return askFine to askBackground
}
