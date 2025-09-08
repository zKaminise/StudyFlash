package com.example.studyflash.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyflash.data.auth.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    val user: StateFlow<FirebaseUser?> =
        repo.userFlow.stateIn(viewModelScope, SharingStarted.Eagerly, repo.currentUser())

    var errorMsg: String? = null
        private set

    fun updateName(name: String, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                errorMsg = null
                repo.updateDisplayName(name)
                onDone()
            } catch (e: Exception) {
                errorMsg = e.localizedMessage ?: "Falha ao atualizar nome"
            }
        }
    }

    fun uploadPhoto(uri: Uri, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                errorMsg = null
                repo.uploadProfilePhotoAndLink(uri)
                onDone()
            } catch (e: Exception) {
                errorMsg = e.localizedMessage ?: "Falha ao enviar foto"
            }
        }
    }
}
