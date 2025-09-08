package com.example.studyflash.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyflash.data.local.FlashcardEntity
import com.example.studyflash.data.repository.FlashcardRepository
import com.example.studyflash.data.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: FlashcardRepository,
    private val sync: SyncManager
) : ViewModel() {

    val items = repo.observeLocal()
        .map { list -> list.sortedByDescending { it.updatedAt } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun add(type: String, front: String?, back: String?) {
        viewModelScope.launch { repo.add(type, front, back) }
    }

    fun delete(card: FlashcardEntity) {
        viewModelScope.launch { repo.delete(card) }
    }

    fun syncPull(onDone: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val n = sync.pullAll()
            onDone(n)
        }
    }

    fun syncPush(onDone: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val n = sync.pushAll()
            onDone(n)
        }
    }
}
