package com.example.studyflash.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HomeScreen(
    padding: PaddingValues,
    onCreate: () -> Unit = {},
    onStudy:  () -> Unit = {},
    onCards:  () -> Unit = {},
    vm: HomeViewModel = hiltViewModel()
) {
    // Você pode exibir contagens aqui no futuro (total/due)
    Scaffold(
        modifier = Modifier.padding(padding),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreate,
                icon = { Icon(Icons.Filled.Add, contentDescription = "Novo") },
                text  = { Text("Novo flashcard") }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Bem-vindo ao StudyFlash!", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = onStudy, modifier = Modifier.fillMaxWidth()) {
                Text("Iniciar Estudo")
            }
            OutlinedButton(onClick = onCards, modifier = Modifier.fillMaxWidth()) {
                Text("Ver Cards")
            }
            // Espaço para métricas e atalhos futuros
            // --- Sync ---
            val host = remember { SnackbarHostState() }
            SnackbarHost(hostState = host)

            OutlinedButton(
                onClick = { vm.syncPull { n ->
                    // feedback simples
                    // você pode trocar por Dialog/Toast
                } },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Sincronizar (Puxar do Servidor)") }

            OutlinedButton(
                onClick = { vm.syncPush { n -> } },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Enviar Tudo (Subir para o Servidor)") }
        }
    }
}
