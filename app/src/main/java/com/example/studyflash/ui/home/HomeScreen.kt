package com.example.studyflash.ui.home


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.studyflash.data.local.FlashcardEntity
import com.example.studyflash.navigation.Dest


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(nav: NavHostController, vm: HomeViewModel = hiltViewModel()) {
    val items by vm.items.collectAsState(initial = emptyList())


    Scaffold(
        topBar = { TopAppBar(title = { Text("StudyFlash — Home") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { nav.navigate(Dest.Create.route) }) {
                Text("Novo Flashcard")
            }
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp)) {
            items(items) { card -> FlashcardRow(card) }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}


@Composable
private fun FlashcardRow(card: FlashcardEntity) {
    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(text = card.type, style = MaterialTheme.typography.titleMedium)
            if (!card.frontText.isNullOrBlank()) Text("Frente: ${card.frontText}")
            if (!card.backText.isNullOrBlank()) Text("Verso: ${card.backText}")
        }
    }
}