package com.example.studyflash.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyflash.data.repository.FlashcardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class CreateFlashcardViewModel @Inject constructor(
    private val repo: FlashcardRepository
) : ViewModel() {

    fun saveMcq(
        question: String,
        correct: String,
        wrong1: String,
        wrong2: String,
        wrong3: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            repo.add(
                type = "mcq",
                front = question,
                back = correct,
                wrong1 = wrong1,
                wrong2 = wrong2,
                wrong3 = wrong3
            )
            onDone()
        }
    }

    fun saveFrontBack(front: String, back: String, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.add(type = "front_back", front = front, back = back)
            onDone()
        }
    }

    fun saveFreeText(question: String, answers: List<String>, onDone: () -> Unit) {
        viewModelScope.launch {
            val back = answers.joinToString(";")
            repo.add(type = "free_text", front = question, back = back)
            onDone()
        }
    }

    fun saveCloze(text: String, answers: List<String>, onDone: () -> Unit) {
        viewModelScope.launch {
            val back = answers.joinToString(";")
            repo.add(type = "cloze", front = text, back = back)
            onDone()
        }
    }
}
