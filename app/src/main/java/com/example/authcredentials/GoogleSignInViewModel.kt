package com.example.authcredentials

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

class GoogleSignInViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun handleGoogleSignIn(context: Context, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            googleSignIn(context).collect { result ->
                result.fold(
                    onSuccess = {
                        Log.d("GoogleSignIn", "Sign-in successful!")
                        onResult(true)
                    },
                    onFailure = { e ->
                        Log.e("GoogleSignIn", "Sign-in failed: ${e.message}")
                        Toast.makeText(context, "Sign-in failed: ${e.message}", Toast.LENGTH_LONG).show()
                        onResult(false)
                    }
                )
            }
        }
    }

    private suspend fun googleSignIn(context: Context): Flow<Result<Boolean>> = callbackFlow {
        try {
            val credentialManager = CredentialManager.create(context)

            val nonce = UUID.randomUUID().toString()
            val digest = MessageDigest.getInstance("SHA-256").digest(nonce.toByteArray())
            val hashedNonce = digest.joinToString("") { "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setNonce(hashedNonce)
                .setAutoSelectEnabled(false) // Ensures account selection prompt
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                auth.signInWithCredential(authCredential).await()
                trySend(Result.success(true))
            } else {
                throw RuntimeException("Invalid credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            trySend(Result.failure(Exception("Sign-in was canceled.")))
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }
}
