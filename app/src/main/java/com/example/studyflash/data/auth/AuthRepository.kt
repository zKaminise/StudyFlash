package com.example.studyflash.data.auth

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) {

    val userFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    fun currentUser(): FirebaseUser? = auth.currentUser

    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun updateDisplayName(name: String) {
        val user = auth.currentUser ?: return
        val req = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        user.updateProfile(req).await()
    }

    suspend fun uploadProfilePhotoAndLink(uri: Uri): String {
        val user = auth.currentUser ?: error("No user")
        val ref = storage.reference.child("users/${user.uid}/profile.jpg")
        ref.putFile(uri).await()
        val url = ref.downloadUrl.await().toString()
        val req = UserProfileChangeRequest.Builder()
            .setPhotoUri(Uri.parse(url))
            .build()
        user.updateProfile(req).await()
        return url
    }
}
