package com.example.studyflash.ui.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyflash.data.local.FlashcardEntity
import com.example.studyflash.data.repository.FlashcardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CardsViewModel @Inject constructor(
    private val repo: FlashcardRepository
) : ViewModel() {

    val cards: StateFlow<List<FlashcardEntity>> =
        repo.observeLocal().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun delete(card: FlashcardEntity, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            runCatching { repo.delete(card) }
                .onSuccess { onDone(true) }
                .onFailure { onDone(false) }
        }
    }
}
