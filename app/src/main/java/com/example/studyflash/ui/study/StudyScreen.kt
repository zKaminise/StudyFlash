package com.example.studyflash.ui.study


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun StudyScreen(nav: NavHostController) {
    Scaffold(topBar = { TopAppBar(title = { Text("Estudar") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Placeholder do modo de estudo (SM-2) — será implementado na próxima etapa.")
            Spacer(Modifier.height(12.dp))
            Button(onClick = { /* TODO */ }) { Text("Iniciar sessão") }
        }
    }
}