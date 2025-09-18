package com.example.studyflash.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyflash.data.local.AttemptHistoryDao
import com.example.studyflash.data.local.LocationStatsRow
import com.example.studyflash.data.repository.LocationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class LocationAnalytics(
    val locationId: String?,   // null = sem local
    val locationName: String,  // resolvido (ex.: "Casa", "Biblioteca", "Sem local")
    val total: Int,
    val correct: Int,
    val accuracyPct: Int,      // 0..100
    val lastAnsweredAt: Long
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val attempts: AttemptHistoryDao,
    private val locationsRepo: LocationsRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<LocationAnalytics>>(emptyList())
    val items: StateFlow<List<LocationAnalytics>> = _items.asStateFlow()

    fun load() {
        viewModelScope.launch {
            val stats = attempts.getStatsByLocation()
            val allLocs = locationsRepo.listAll().associateBy { it.id }

            val mapped = stats.map { row ->
                toAnalytics(row, allLocs[row.locationId]?.name)
            }
            _items.value = mapped
        }
    }

    private fun toAnalytics(row: LocationStatsRow, resolvedName: String?): LocationAnalytics {
        val pct = if (row.total > 0) ((row.correctCount.toDouble() / row.total) * 100).roundToInt() else 0
        val name = resolvedName ?: "Sem local"
        return LocationAnalytics(
            locationId = row.locationId,
            locationName = name,
            total = row.total,
            correct = row.correctCount,
            accuracyPct = pct,
            lastAnsweredAt = row.lastAnsweredAt
        )
    }
}
