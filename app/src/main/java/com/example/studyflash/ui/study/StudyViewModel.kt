package com.example.studyflash.ui.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyflash.data.local.FlashcardEntity
import com.example.studyflash.data.repository.FlashcardRepository
import com.example.studyflash.data.repository.LocationsRepository
import com.example.studyflash.domain.spaced.StudyGrade
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class StudyViewModel @Inject constructor(
    private val repo: FlashcardRepository,
    private val locationsRepo: LocationsRepository
) : ViewModel() {

    private val _current = MutableStateFlow<FlashcardEntity?>(null)
    val current: StateFlow<FlashcardEntity?> = _current.asStateFlow()

    private val _options = MutableStateFlow<List<String>>(emptyList())
    val options: StateFlow<List<String>> = _options.asStateFlow()

    private val _selectedIndex = MutableStateFlow<Int?>(null)
    val selectedIndex: StateFlow<Int?> = _selectedIndex.asStateFlow()

    private val _isCorrect = MutableStateFlow<Boolean?>(null)
    val isCorrect: StateFlow<Boolean?> = _isCorrect.asStateFlow()

    val manualResult = MutableStateFlow<Boolean?>(null)

    // --- Progresso da sessão ---
    private val _sessionTotal = MutableStateFlow(0)    // total devido no momento de iniciar
    val sessionTotal: StateFlow<Int> = _sessionTotal.asStateFlow()

    private val _answeredCount = MutableStateFlow(0)   // quantos já foram confirmados nesta sessão
    val answeredCount: StateFlow<Int> = _answeredCount.asStateFlow()

    private var sessionStarted = false

    private var startTimeMs: Long = 0L
    private val now: Long get() = System.currentTimeMillis()

    fun startOrContinue() {
        viewModelScope.launch {
            if (!sessionStarted) {
                sessionStarted = true
                _answeredCount.value = 0
                _sessionTotal.value = repo.countDue() // total no início da sessão
            }
            loadNextInternal()
        }
    }

    fun reloadNext() {
        viewModelScope.launch { loadNextInternal() }
    }

    private suspend fun loadNextInternal() {
        val currentLoc = locationsRepo.getCurrentLocationId()
        val card = repo.getNextDue(currentLoc)
        _current.value = card
        _selectedIndex.value = null
        _isCorrect.value = null
        manualResult.value = null
        _options.value = if (card != null && card.type == "mcq") repo.buildOptionsFor(card) else emptyList()
        startTimeMs = now
    }

    fun choose(index: Int) {
        val card = _current.value ?: return
        val opts = _options.value
        if (index !in opts.indices) return
        _selectedIndex.value = index
        val correct = (card.backText ?: "").ifBlank { "Sem resposta" }
        _isCorrect.value = (opts[index] == correct)
    }

    fun commitAnswer(onAfter: () -> Unit = {}) {
        val card = _current.value ?: return
        val elapsed = (now - startTimeMs).coerceAtLeast(0L)
        viewModelScope.launch {
            val currentLoc = locationsRepo.getCurrentLocationId()
            val correct = (_isCorrect.value == true)
            repo.recordReview(
                card = card,
                grade = if (correct) StudyGrade.Good else StudyGrade.Again,
                timeToAnswerMs = elapsed,
                currentLocationId = currentLoc
            )
            _answeredCount.value = (_answeredCount.value + 1).coerceAtLeast(0)
            onAfter()
            loadNextInternal()
        }
    }

    fun commitGrade(grade: StudyGrade, onAfter: () -> Unit = {}) {
        val card = _current.value ?: return
        val elapsed = (now - startTimeMs).coerceAtLeast(0L)
        viewModelScope.launch {
            val currentLoc = locationsRepo.getCurrentLocationId()
            repo.recordReview(
                card = card,
                grade = grade,
                timeToAnswerMs = elapsed,
                currentLocationId = currentLoc
            )
            _answeredCount.value = (_answeredCount.value + 1).coerceAtLeast(0)
            onAfter()
            loadNextInternal()
        }
    }
}
