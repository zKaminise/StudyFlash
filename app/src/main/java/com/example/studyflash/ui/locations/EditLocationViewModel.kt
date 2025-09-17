package com.example.studyflash.ui.locations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyflash.data.local.FavoriteLocationEntity
import com.example.studyflash.data.repository.LocationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class EditLocationViewModel @Inject constructor(
    private val repo: LocationsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<FavoriteLocationEntity?>(null)
    val state: StateFlow<FavoriteLocationEntity?> = _state

    fun load(id: String) {
        viewModelScope.launch {
            _state.value = repo.getById(id)
        }
    }

    fun save(id: String, lat: Double, lon: Double, radius: Float, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            runCatching {
                repo.updateCoordinates(id, lat, lon)
                repo.updateRadius(id, radius)
            }.onSuccess { onDone(true) }
                .onFailure { onDone(false) }
        }
    }
}
