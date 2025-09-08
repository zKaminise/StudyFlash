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

    fun save(type: String, front: String?, back: String?) {
        viewModelScope.launch {
            repo.add(type, front, back)
        }
    }
}
