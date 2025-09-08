package com.example.studyflash.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage

@Composable
fun ProfileScreen(
    padding: PaddingValues,
    vm: ProfileViewModel = hiltViewModel()
) {
    val user by vm.user.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf(TextFieldValue(user?.displayName.orEmpty())) }

    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { vm.uploadPhoto(it) { /* feedback opcional */ } }
    }

    Scaffold { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Meu perfil", style = MaterialTheme.typography.titleLarge)

            AsyncImage(
                model = user?.photoUrl,
                contentDescription = "Foto de perfil",
                modifier = Modifier.size(120.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    pickPhotoLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) { Text("Trocar foto") }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome de usuário") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { vm.updateName(name.text) { /* feedback opcional */ } },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Salvar nome") }

            vm.errorMsg?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
