package com.example.studyflash.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    padding: PaddingValues,
    onCreate: () -> Unit = {},
    onStudy:  () -> Unit = {},
    onCards:  () -> Unit = {},
    vm: HomeViewModel = hiltViewModel()
) {
    val host = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.padding(padding),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreate,
                icon = { Icon(Icons.Filled.Add, contentDescription = "Novo") },
                text  = { Text("Novo flashcard") }
            )
        },
        snackbarHost = { SnackbarHost(hostState = host) }
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

            Divider(Modifier.padding(vertical = 8.dp))

            OutlinedButton(
                onClick = {
                    vm.syncPull { n ->
                        scope.launch {
                            host.showSnackbar("Baixados do servidor: $n cards")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Baixar do servidor") }

            OutlinedButton(
                onClick = {
                    vm.syncPush { n ->
                        scope.launch {
                            host.showSnackbar("Enviados ao servidor: $n cards")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Enviar ao servidor") }
        }
    }
}
