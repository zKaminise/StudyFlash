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

    // Mensagem para Snackbar/Toast na UI
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    val items = repo.observeLocal()
        .map { list -> list.sortedByDescending { it.updatedAt } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _summary = MutableStateFlow(HomeSummary())
    val summary: StateFlow<HomeSummary> = _summary

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

    fun consumeMessage(): String? { // chame na UI após exibir
        val m = _message.value
        _message.value = null
        return m
    }
    fun syncPull(onDone: (Int, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val res = sync.pullAll()
            res.onSuccess { n ->
                refreshSummary()
                onDone(n, null)
            }.onFailure { e ->
                val msg = e.localizedMessage ?: "Falha de rede. Verifique se o servidor Ktor está rodando em 10.0.2.2:8080."
                onDone(0, msg)
            }
        }
    }

    fun syncPush(onDone: (Int, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val res = sync.pushAll()
            res.onSuccess { n ->
                refreshSummary()
                onDone(n, null)
            }.onFailure { e ->
                val msg = e.localizedMessage ?: "Falha de rede ao enviar. Confirme o servidor Ktor."
                onDone(0, msg)
            }
        }
    }
}

