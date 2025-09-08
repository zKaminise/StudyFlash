package com.example.studyflash.ui.cards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studyflash.ui.home.HomeViewModel

@Composable
fun CardsScreen(
    padding: PaddingValues,
    vm: HomeViewModel = hiltViewModel()
) {
    val items by vm.items.collectAsState()

    Column(
        Modifier
            .padding(padding)
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Text("Seus Cards", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { card ->
                Card {
                    Column(Modifier.padding(12.dp)) {
                        Text("Pergunta:", style = MaterialTheme.typography.labelLarge)
                        Text(card.frontText ?: "(sem texto)")
                        Spacer(Modifier.height(6.dp))
                        Text("Resposta:", style = MaterialTheme.typography.labelLarge)
                        Text(card.backText ?: "(sem resposta)")
                    }
                }
            }
        }
    }
}
