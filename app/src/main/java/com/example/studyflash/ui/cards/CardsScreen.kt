package com.example.studyflash.ui.cards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studyflash.data.local.FlashcardEntity
import kotlinx.coroutines.launch

@Composable
fun CardsScreen(
    padding: PaddingValues,
    vm: CardsViewModel = hiltViewModel()
) {
    val cards by vm.cards.collectAsState()
    val host = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var pendingDelete by remember { mutableStateOf<FlashcardEntity?>(null) }

    Scaffold(
        modifier = Modifier.padding(padding),
        snackbarHost = { SnackbarHost(host) }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = cards, key = { it.id }) { card ->
                FlashcardRow(
                    card = card,
                    onDeleteClick = { pendingDelete = card }
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    // Diálogo de confirmação
    val toDelete = pendingDelete
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Excluir flashcard") },
            text = { Text("Tem certeza que deseja excluir este card? Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.delete(toDelete) { ok ->
                        pendingDelete = null
                        scope.launch {
                            host.showSnackbar(
                                if (ok) "Card excluído"
                                else "Falha ao excluir"
                            )
                        }
                    }
                }) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun FlashcardRow(
    card: FlashcardEntity,
    onDeleteClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = card.type.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = card.frontText ?: "[Sem pergunta]",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = card.backText ?: "[Sem resposta]",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                }
            }
            Spacer(Modifier.height(8.dp))
            Divider()
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Atualizado: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
                    .format(java.util.Date(card.updatedAt))}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
