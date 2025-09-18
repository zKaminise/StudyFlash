package com.example.studyflash.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    padding: PaddingValues,
    onCreate: () -> Unit = {},
    onStudy:  () -> Unit = {},
    onCards:  () -> Unit = {},
    onLocations: () -> Unit = {},
    onAnalytics: () -> Unit = {},
    vm: HomeViewModel = hiltViewModel()
) {
    val host = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val summary by vm.summary.collectAsStateWithLifecycle()
    var menuOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.refreshSummary() }

    Scaffold(
        modifier = Modifier.padding(padding),
        topBar = {
            TopAppBar(
                title = { Text("StudyFlash") },
                actions = {
                    IconButton(onClick = { vm.refreshSummary() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Atualizar")
                    }
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Filled.CloudUpload, contentDescription = "Sincronizar")
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Baixar do servidor") },
                                leadingIcon = { Icon(Icons.Filled.CloudDownload, contentDescription = null) },
                                onClick = {
                                    menuOpen = false
                                    vm.syncPull { count, error ->
                                        scope.launch {
                                            host.showSnackbar(
                                                error ?: "Baixados do servidor: $count cards"
                                            )
                                        }
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Enviar ao servidor") },
                                leadingIcon = { Icon(Icons.Filled.CloudUpload, contentDescription = null) },
                                onClick = {
                                    menuOpen = false
                                    vm.syncPush { count, error ->
                                        scope.launch {
                                            host.showSnackbar(
                                                error ?: "Enviados ao servidor: $count cards"
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryCard(total = summary.total, due = summary.due)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = onLocations,
                    label = { Text("Trocar local de estudo") },
                    leadingIcon = { Icon(Icons.Filled.Place, contentDescription = null) }
                )
            }

            Divider()

            Button(onClick = onStudy, modifier = Modifier.fillMaxWidth()) {
                Text("Iniciar Estudo")
            }
            Button(onClick = onCards, modifier = Modifier.fillMaxWidth()) {
                Text("Ver Cards")
            }
            Button(onClick = onLocations, modifier = Modifier.fillMaxWidth()) {
                Text("Locais de Estudo")
            }
            // ✅ apenas um botão para a mesma tela
            Button(onClick = onAnalytics, modifier = Modifier.fillMaxWidth()) {
                Text("Analytics (por local)")
            }
        }
    }
}

@Composable
private fun SummaryCard(total: Int, due: Int) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Resumo", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        "Total",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("$total", style = MaterialTheme.typography.headlineSmall)
                }
                Column {
                    Text(
                        "Devidos",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$due",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (due > 0) "Você tem $due para estudar agora." else "Nada devido por enquanto.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start
            )
        }
    }
}
