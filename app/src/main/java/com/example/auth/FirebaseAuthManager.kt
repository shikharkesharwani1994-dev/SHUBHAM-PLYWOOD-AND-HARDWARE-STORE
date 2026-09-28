package com.example.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class AuthResult {
    data class Success(val user: FirebaseUser?) : AuthResult()
    data class Error(val message: String, val isConfigMissing: Boolean = false) : AuthResult()
    object Cancelled : AuthResult()
}

class FirebaseAuthManager(private val context: Context) {

    private val tag = "FirebaseAuthManager"
    private var auth: FirebaseAuth? = null

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isFirebaseConfigured = MutableStateFlow(false)
    val isFirebaseConfigured: StateFlow<Boolean> = _isFirebaseConfigured.asStateFlow()

    init {
        initFirebase()
    }

    private fun initFirebase() {
        try {
            val apps = FirebaseApp.getApps(context)
            if (apps.isNotEmpty()) {
                auth = FirebaseAuth.getInstance()
                _isFirebaseConfigured.value = true
                _currentUser.value = auth?.currentUser
                auth?.addAuthStateListener { firebaseAuth ->
                    _currentUser.value = firebaseAuth.currentUser
                }
            } else {
                _isFirebaseConfigured.value = false
                Log.w(tag, "FirebaseApp is not initialized. google-services.json may be pending.")
            }
        } catch (e: Exception) {
            _isFirebaseConfigured.value = false
            Log.w(tag, "FirebaseAuth initialization notice: ${e.message}")
        }
    }

    suspend fun signInWithGoogle(activity: Activity, webClientId: String = ""): AuthResult = withContext(Dispatchers.IO) {
        try {
            val credentialManager = CredentialManager.create(activity)

            val googleIdOptionBuilder = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)

            // If webClientId is provided (from Firebase Console), attach it
            if (webClientId.isNotBlank()) {
                googleIdOptionBuilder.setServerClientId(webClientId)
            } else {
                // Fallback default server client ID format for developer convenience
                googleIdOptionBuilder.setServerClientId("203914155776-auth.apps.googleusercontent.com")
            }

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOptionBuilder.build())
                .build()

            val response: GetCredentialResponse = try {
                credentialManager.getCredential(activity, request)
            } catch (e: GetCredentialCancellationException) {
                return@withContext AuthResult.Cancelled
            } catch (e: GetCredentialException) {
                return@withContext AuthResult.Error("Google Sign-In prompt error: ${e.localizedMessage ?: e.message}")
            }

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val firebaseAuth = auth
                if (firebaseAuth != null) {
                    val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                    AuthResult.Success(authResult.user)
                } else {
                    AuthResult.Error(
                        "Google token obtained successfully! To link with Firebase, ensure google-services.json is configured.",
                        isConfigMissing = true
                    )
                }
            } else {
                AuthResult.Error("Unexpected credential type returned from Google Sign-In.")
            }
        } catch (e: Exception) {
            Log.e(tag, "Google Sign-In failed", e)
            AuthResult.Error(e.localizedMessage ?: "Google Sign-In failed")
        }
    }

    suspend fun signInWithEmail(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            return@withContext AuthResult.Error(
                "Firebase Auth is ready. Add google-services.json to authenticate against your live Firebase cloud project.",
                isConfigMissing = true
            )
        }
        try {
            val result = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
            AuthResult.Success(result.user)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Email sign-in failed")
        }
    }

    suspend fun signUpWithEmail(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            return@withContext AuthResult.Error(
                "Firebase Auth is ready. Add google-services.json to create user accounts in Firebase.",
                isConfigMissing = true
            )
        }
        try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
            AuthResult.Success(result.user)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Account creation failed")
        }
    }

    suspend fun signInAnonymously(): AuthResult = withContext(Dispatchers.IO) {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            return@withContext AuthResult.Error("Firebase not initialized yet.", isConfigMissing = true)
        }
        try {
            val result = firebaseAuth.signInAnonymously().await()
            AuthResult.Success(result.user)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Guest sign-in failed")
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
            _currentUser.value = null
        } catch (e: Exception) {
            Log.e(tag, "Sign out error", e)
        }
    }
}
