package com.example.studyflash.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyflash.data.local.AttemptHistoryDao
import com.example.studyflash.data.local.AttemptHistoryEntity
import com.example.studyflash.data.local.LocationStatsRow
import com.example.studyflash.data.repository.LocationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.roundToInt

data class LocationStatsUI(
    val locationName: String, // "(Sem local)" se null
    val total: Int,
    val correct: Int,
    val pct: Int,
    val avgMs: Long
)

data class HeatBucket(
    val weekLabel: String, // "Wk NN"
    val locationName: String,
    val total: Int,
    val correct: Int
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val attempts: AttemptHistoryDao,
    private val locationsRepo: LocationsRepository
) : ViewModel() {

    private val _rangeDays = MutableStateFlow(30) // 7 / 30 / 90
    val rangeDays: StateFlow<Int> = _rangeDays

    private val _byLocation = MutableStateFlow<List<LocationStatsUI>>(emptyList())
    val byLocation: StateFlow<List<LocationStatsUI>> = _byLocation

    private val _heatmap = MutableStateFlow<List<HeatBucket>>(emptyList())
    val heatmap: StateFlow<List<HeatBucket>> = _heatmap

    init { refresh() }

    fun setRange(days: Int) {
        _rangeDays.value = days
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val from = now - _rangeDays.value * 24L * 60 * 60 * 1000

            // Mapa de nomes de locais
            val locMap = locationsRepo.listAll().associateBy({ it.id }, { it.name })

            // 1) Agregado por local
            val rows: List<LocationStatsRow> = attempts.getStatsByLocation(from, now)
            _byLocation.value = rows.map { r ->
                val pct = if (r.total > 0) ((r.correctCount * 100.0) / r.total).roundToInt() else 0
                LocationStatsUI(
                    locationName = locMap[r.locationId] ?: "(Sem local)",
                    total = r.total,
                    correct = r.correctCount,
                    pct = pct,
                    avgMs = r.avgTimeMs ?: 0L
                )
            }

            // 2) Heatmap semanal (a partir de answeredAt)
            val raw: List<AttemptHistoryEntity> = attempts.listBetween(from, now)
            _heatmap.value = buildWeeklyHeatmap(raw, locMap)
        }
    }

    private fun buildWeeklyHeatmap(
        items: List<AttemptHistoryEntity>,
        locMap: Map<String, String>
    ): List<HeatBucket> {
        if (items.isEmpty()) return emptyList()
        val zone = ZoneId.systemDefault()
        val weekFields = WeekFields.of(Locale.getDefault())

        data class Key(val week: Int, val year: Int, val loc: String)

        val byKey = items.groupBy { a ->
            val date = Instant.ofEpochMilli(a.answeredAt).atZone(zone).toLocalDate()
            val week = date.get(weekFields.weekOfWeekBasedYear())
            val year = date.get(weekFields.weekBasedYear())
            val locName = locMap[a.locationId] ?: "(Sem local)"
            Key(week, year, locName)
        }

        return byKey.entries.map { (k, list) ->
            val total = list.size
            val correct = list.count { it.correct }
            HeatBucket(
                weekLabel = "Wk ${k.week.toString().padStart(2, '0')}",
                locationName = k.loc,
                total = total,
                correct = correct
            )
        }.sortedWith(compareBy<HeatBucket> { it.weekLabel }.thenBy { it.locationName })
            .takeLast(10)
    }
}
