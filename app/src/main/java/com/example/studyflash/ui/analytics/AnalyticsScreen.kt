package com.example.studyflash.ui.analytics


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AnalyticsScreen(nav: NavHostController) {
    Scaffold(topBar = { TopAppBar(title = { Text("Analytics") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Placeholder de painéis analíticos — sessões por local/horário/tipo.")
        }
    }
}