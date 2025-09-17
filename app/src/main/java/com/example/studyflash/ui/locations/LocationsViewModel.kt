package com.example.studyflash.ui.locations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyflash.data.local.FavoriteLocationEntity
import com.example.studyflash.data.repository.LocationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class LocationsViewModel @Inject constructor(
    private val repo: LocationsRepository
) : ViewModel() {

    val items: StateFlow<List<FavoriteLocationEntity>> =
        repo.observe()
            .map { it.sortedBy { loc -> loc.name.lowercase() } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val currentId: StateFlow<String?> =
        repo.observe()
            .map { list -> list.firstOrNull { it.isCurrent }?.id }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun add(name: String, onDone: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val res = repo.add(name)
            onDone(res.isSuccess, res.exceptionOrNull()?.message)
        }
    }

    fun delete(id: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repo.delete(id)
            onDone()
        }
    }

    fun setCurrent(id: String) {
        viewModelScope.launch { repo.setCurrent(id) }
    }

    fun updateCoordinates(id: String, lat: Double, lon: Double, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            runCatching { repo.updateCoordinates(id, lat, lon) }
                .onSuccess { onDone(true) }
                .onFailure { onDone(false) }
        }
    }
}
