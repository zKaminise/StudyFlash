package com.example.studyflash.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studyflash.data.local.FlashcardEntity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row

@Composable
fun HomeScreen(
    padding: PaddingValues,
    onCreate: () -> Unit,
    vm: HomeViewModel = hiltViewModel()
) {
    val items by vm.items.collectAsState()

    Scaffold(
        modifier = Modifier.padding(padding),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreate,
                icon = { Icon(Icons.Filled.Add, contentDescription = "Novo") },
                text = { Text("Novo flashcard") }
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier.padding(inner),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { card ->
                FlashcardRow(card, onDelete = { vm.delete(card) })
            }
        }
    }
}

@Composable
private fun FlashcardRow(card: FlashcardEntity, onDelete: () -> Unit) {
    Card {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tipo: ${card.type}", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                }
            }
            if (!card.frontText.isNullOrBlank()) Text("Frente: ${card.frontText}")
            if (!card.backText.isNullOrBlank()) Text("Verso: ${card.backText}")
        }
    }
}
