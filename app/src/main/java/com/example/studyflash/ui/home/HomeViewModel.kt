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

    fun syncPull(onDone: (Int) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val n = sync.pullAll()
                refreshSummary()
                _message.value = "Baixados $n cards do servidor."
                onDone(n)
            } catch (e: Exception) {
                _message.value = "Falha ao baixar: verifique se o servidor está rodando."
                onDone(0)
            }
        }
    }

    fun syncPush(onDone: (Int) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val n = sync.pushAll()
                refreshSummary()
                _message.value = "Enviados $n cards ao servidor."
                onDone(n)
            } catch (e: Exception) {
                _message.value = "Falha ao enviar: verifique sua conexão/servidor."
                onDone(0)
            }
        }
    }
}

