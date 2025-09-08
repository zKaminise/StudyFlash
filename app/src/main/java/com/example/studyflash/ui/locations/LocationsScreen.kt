package com.example.studyflash.ui.locations


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LocationsScreen(nav: NavHostController) {
    Scaffold(topBar = { TopAppBar(title = { Text("Locais Favoritos") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Placeholder de GPS/Geofencing — adicionaremos CRUD de até 7 locais.")
            Spacer(Modifier.height(12.dp))
            Button(onClick = { /* TODO */ }) { Text("Adicionar Local") }
        }
    }
}