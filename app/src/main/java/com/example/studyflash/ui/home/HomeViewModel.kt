package com.example.studyflash.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyflash.data.local.FlashcardEntity
import com.example.studyflash.data.repository.FlashcardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: FlashcardRepository
) : ViewModel() {

    val items: StateFlow<List<FlashcardEntity>> =
        repo.observeLocal()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(type: String, front: String?, back: String?) {
        viewModelScope.launch { repo.add(type, front, back) }
    }

    fun delete(card: FlashcardEntity) {
        viewModelScope.launch { repo.delete(card) }
    }
}
