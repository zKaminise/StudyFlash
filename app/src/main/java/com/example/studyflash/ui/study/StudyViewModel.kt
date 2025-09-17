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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class StudyViewModel @Inject constructor(
    private val repo: FlashcardRepository,
    private val locationsRepo: LocationsRepository
) : ViewModel() {

    private val _current = MutableStateFlow<FlashcardEntity?>(null)
    val current = _current.asStateFlow()

    private val _options = MutableStateFlow<List<String>>(emptyList())
    val options = _options.asStateFlow()

    private val _selectedIndex = MutableStateFlow<Int?>(null)
    val selectedIndex = _selectedIndex.asStateFlow()

    private val _isCorrect = MutableStateFlow<Boolean?>(null)
    val isCorrect = _isCorrect.asStateFlow()

    val manualResult = MutableStateFlow<Boolean?>(null)

    private var startTimeMs: Long = 0L
    private val now: Long get() = System.currentTimeMillis()

    fun loadNext() {
        viewModelScope.launch {
            val currentLoc = locationsRepo.getCurrentLocationId()
            val card = repo.getNextDue(currentLoc)
            _current.value = card
            _selectedIndex.value = null
            _isCorrect.value = null
            manualResult.value = null
            _options.value = if (card != null && card.type == "mcq") repo.buildOptionsFor(card) else emptyList()
            startTimeMs = now
        }
    }

    fun choose(index: Int) {
        val card = _current.value ?: return
        val opts = _options.value
        if (index !in opts.indices) return
        _selectedIndex.value = index
        val correct = (card.backText ?: "").ifBlank { "Sem resposta" }
        _isCorrect.value = (opts[index] == correct)
    }

    fun commitAnswer(onAfter: () -> Unit) {
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
            onAfter()
            loadNext()
        }
    }

    // ⬇️ Para tipos manuais: commit com nota explícita
    fun commitGrade(grade: StudyGrade, onAfter: () -> Unit) {
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
            onAfter()
            loadNext()
        }
    }
}
