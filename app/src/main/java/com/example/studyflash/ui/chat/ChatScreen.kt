package com.example.studyflash.ui.chat


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ChatScreen(nav: NavHostController) {
    Scaffold(topBar = { TopAppBar(title = { Text("Tutor IA") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Placeholder do chat de IA — conectaremos ao Ktor na etapa de IA.")
        }
    }
}