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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AnalyticsScreen(
    padding: PaddingValues,
    vm: AnalyticsViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { vm.load() }
    val items = vm.items.collectAsStateWithLifecycle().value

    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Analytics por Local", style = MaterialTheme.typography.titleLarge)

        if (items.isEmpty()) {
            Text(
                "Sem dados ainda. Estude alguns cards para aparecerem estatísticas.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items, key = { (it.locationId ?: "no_loc") + "_" + it.lastAnsweredAt }) { it ->
                AnalyticsCard(it)
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun AnalyticsCard(item: LocationAnalytics) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(item.locationName, style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total: ${item.total}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Acertos: ${item.correct}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${item.accuracyPct}%", color = MaterialTheme.colorScheme.primary)
            }
            LinearProgressIndicator(
                progress = { item.accuracyPct / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            )
        }
    }
}
