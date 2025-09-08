package com.example.studyflash.ui.home


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyflash.data.repository.FlashcardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    repo: FlashcardRepository
) : ViewModel() {
    val items = repo.observeLocal().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}