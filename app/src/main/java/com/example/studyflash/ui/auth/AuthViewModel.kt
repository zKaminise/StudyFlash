package com.example.studyflash.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    val user: StateFlow<FirebaseUser?> =
        repo.userFlow.stateIn(viewModelScope, SharingStarted.Eagerly, repo.currentUser())

    var errorMsg: String? by mutableStateOf(null)
        private set

    fun setError(msg: String) { errorMsg = msg }
    fun clearError() { errorMsg = null }

    fun signIn(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                clearError()
                repo.signIn(email, pass)
                onSuccess()
            } catch (e: Exception) {
                setError(e.localizedMessage ?: "Falha ao entrar")
            }
        }
    }

    fun signUp(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                clearError()
                repo.signUp(email, pass)
                onSuccess()
            } catch (e: Exception) {
                setError(e.localizedMessage ?: "Falha ao cadastrar")
            }
        }
    }

    fun signOut(onAfter: () -> Unit) {
        repo.signOut()
        onAfter()
    }

    // ⬇️ NOVO
    fun sendPasswordReset(email: String, onDone: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                clearError()
                repo.sendPasswordReset(email)
                onDone(true, null)
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Falha ao enviar e-mail de recuperação"
                setError(msg)
                onDone(false, msg)
            }
        }
    }
}
