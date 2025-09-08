package com.example.studyflash.ui.auth

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
        repo.authState().stateIn(viewModelScope, SharingStarted.Eagerly, repo.currentUser)

    var errorMsg: String? = null
        private set

    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            runCatching { repo.signIn(email, password) }
                .onSuccess { onSuccess() }
                .onFailure { errorMsg = it.message }
        }
    }

    fun signUp(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            runCatching { repo.signUp(email, password) }
                .onSuccess { onSuccess() }
                .onFailure { errorMsg = it.message }
        }
    }

    fun signOut() { repo.signOut() }
}
