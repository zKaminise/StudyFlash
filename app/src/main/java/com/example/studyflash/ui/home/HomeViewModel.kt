package com.example.studyflash.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyflash.data.local.FlashcardEntity
import com.example.studyflash.data.repository.FlashcardRepository
import com.example.studyflash.data.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeSummary(val total: Int = 0, val due: Int = 0)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: FlashcardRepository,
    private val sync: SyncManager
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    val items = repo.observeLocal()
        .map { list -> list.sortedByDescending { it.updatedAt } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _summary = MutableStateFlow(HomeSummary())
    val summary: StateFlow<HomeSummary> = _summary

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    fun add(type: String, front: String?, back: String?) {
        viewModelScope.launch { repo.add(type, front, back) }
    }

    fun delete(card: FlashcardEntity) {
        viewModelScope.launch { repo.delete(card) }
    }

    fun refreshSummary() {
        viewModelScope.launch {
            val total = repo.countAll()
            val due = repo.countDue()
            _summary.value = HomeSummary(total = total, due = due)
        }
    }

    fun consumeMessage(): String? {
        val m = _message.value
        _message.value = null
        return m
    }

    fun syncPull(onDone: (Int, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _isSyncing.value = true
            val res = sync.pullAll()
            _isSyncing.value = false
            res.onSuccess { n ->
                refreshSummary()
                onDone(n, null)
            }.onFailure { e ->
                val msg = e.localizedMessage ?: "Falha de rede. Verifique o servidor."
                onDone(0, msg)
            }
        }
    }

    /** Push seletivo: só envia updatedAt > lastSyncAt */
    fun syncPush(onDone: (Int, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _isSyncing.value = true
            val res = sync.pushSinceLastSync()
            _isSyncing.value = false
            res.onSuccess { n ->
                refreshSummary()
                onDone(n, null)
            }.onFailure { e ->
                val msg = e.localizedMessage ?: "Falha de rede ao enviar."
                onDone(0, msg)
            }
        }
    }
}
