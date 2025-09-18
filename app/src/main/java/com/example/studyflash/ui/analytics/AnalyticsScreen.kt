package com.example.studyflash.ui.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AnalyticsScreen(
    padding: PaddingValues,
    vm: AnalyticsViewModel = hiltViewModel()
) {
    // VM já faz refresh no init, mas não custa nada
    LaunchedEffect(Unit) { vm.refresh() }

    val rangeDays by vm.rangeDays.collectAsStateWithLifecycle()
    val byLocation = vm.byLocation.collectAsStateWithLifecycle().value

    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Analytics por Local", style = MaterialTheme.typography.titleLarge)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = { vm.setRange(7) },
                label = { Text("7d") }
            )
            AssistChip(
                onClick = { vm.setRange(30) },
                label = { Text("30d") }
            )
            AssistChip(
                onClick = { vm.setRange(90) },
                label = { Text("90d") }
            )
        }

        Text(
            "Período: últimos $rangeDays dias",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider()

        if (byLocation.isEmpty()) {
            Text(
                "Sem dados ainda. Estude alguns cards para aparecerem estatísticas.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(
                items = byLocation,
                key = { it.locationName + "_" + it.total + "_" + it.correct }
            ) { item ->
                LocationCard(
                    title = item.locationName,
                    total = item.total,
                    correct = item.correct,
                    pct = item.pct,
                    avgMs = item.avgMs
                )
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun LocationCard(
    title: String,
    total: Int,
    correct: Int,
    pct: Int,
    avgMs: Long
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total: $total", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Acertos: $correct", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$pct%", color = MaterialTheme.colorScheme.primary)
            }

            LinearProgressIndicator(
                progress = { pct / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            )

            Text(
                "Tempo médio: ${avgMs}ms",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
