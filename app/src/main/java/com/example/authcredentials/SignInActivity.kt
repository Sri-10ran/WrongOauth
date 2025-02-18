//package com.example.authcredentials
//
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.credentials.*
//import androidx.credentials.exceptions.GetCredentialException
//import androidx.lifecycle.lifecycleScope
//import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
//import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.GoogleAuthProvider
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.runBlocking
//
//class SignInActivity : ComponentActivity() {
//    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
//    private val credentialManager: CredentialManager by lazy { CredentialManager.create(this) }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            SignInScreen()
//        }
//    }
//
//    @Composable
//    fun SignInScreen() {
//        val context = LocalContext.current
//        var errorMessage by remember { mutableStateOf<String?>(null) }
//
//        Column(
//            modifier = Modifier.fillMaxSize(),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text("Welcome to Laundryes", style = MaterialTheme.typography.headlineSmall)
//            Spacer(modifier = Modifier.height(20.dp))
//            Button(onClick = { signInWithGoogle() }) {
//                Text("Sign In")
//            }
//
//            errorMessage?.let {
//                Spacer(modifier = Modifier.height(10.dp))
//                Text(it, color = MaterialTheme.colorScheme.error)
//            }
//        }
//    }
//
//    private fun signInWithGoogle() {
//        lifecycleScope.launch {
//            try {
//                val googleSignInOption = GetSignInWithGoogleOption.Builder("681121336544-2gsqhul7d8inkrhll4i1a6182ggtlsnr.apps.googleusercontent.com")
//                    .build()
//
//                val request = GetCredentialRequest.Builder()
//                    .addCredentialOption(googleSignInOption)
//                    .build()
//
//                val response = credentialManager.getCredential(
//                    request = request,
//                    context = this@SignInActivity
//                )
//
//                handleSignInResult(response.credential)
//
//            } catch (e: GetCredentialException) {
//                Log.e("GoogleSignIn", "Sign-in failed: ${e.localizedMessage}")
//            }
//        }
//    }
//
//    private fun handleSignInResult(credential: Credential) {
//        when (credential) {
//            is GoogleIdTokenCredential -> {
//                val idToken = credential.idToken
//                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
//
//                auth.signInWithCredential(firebaseCredential)
//                    .addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            Log.d("GoogleSignIn", "Sign-in success!")
//                            startActivity(Intent(this, MainActivity::class.java))  // Change to your success screen
//                            finish()  // Finish current activity so user doesn't return back here
//                        } else {
//                            Log.e("GoogleSignIn", "Firebase sign-in failed", task.exception)
//                            startActivity(Intent(this, LooseActivity::class.java))  // Failure screen
//                            finish()
//                        }
//                    }
//            }
//            else -> {
//                Log.e("GoogleSignIn", "Unsupported credential type.")
//                startActivity(Intent(this, LooseActivity::class.java))
//                finish()
//            }
//        }
//    }
//
//
//    private fun navigateToHome() {
//        startActivity(Intent(this, MainActivity::class.java))
//        finish()
//    }
//
//    private fun navigateToFailure() {
//        startActivity(Intent(this, LooseActivity::class.java))
//        finish()
//    }
//}
package com.example.authcredentials

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.*
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class SignInActivity : ComponentActivity() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val credentialManager: CredentialManager by lazy { CredentialManager.create(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SignInScreen()
        }
    }

    @Composable
    fun SignInScreen() {
        val context = LocalContext.current
        var errorMessage by remember { mutableStateOf<String?>(null) }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to Laundryes", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { signInWithGoogle() }) {
                Text("Sign In")
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }

    private fun signInWithGoogle() {
        lifecycleScope.launch {
            try {
                val googleSignInOption = GetSignInWithGoogleOption.Builder()

                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleSignInOption)
                    .build()

                val response = credentialManager.getCredential(
                    request = request,
                    context = this@SignInActivity
                )

                handleSignInResult(response.credential)

            } catch (e: GetCredentialException) {
                Log.e("GoogleSignIn", "Sign-in failed: ${e.localizedMessage}")

                if (e.localizedMessage?.contains("cancelled", ignoreCase = true) == true) {
                    Toast.makeText(this@SignInActivity, "Sign-in canceled by user.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@SignInActivity, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleSignInResult(credential: Credential) {
        when (credential) {
            is GoogleIdTokenCredential -> {
                val idToken = credential.idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("GoogleSignIn", "Sign-in success!")
                            navigateToHome()
                        } else {
                            Log.e("GoogleSignIn", "Firebase sign-in failed", task.exception)
                            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                            navigateToFailure()
                        }
                    }
            }
            else -> {
                Log.e("GoogleSignIn", "Unsupported credential type.")
                navigateToFailure()
            }
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun navigateToFailure() {
        startActivity(Intent(this, LooseActivity::class.java))
        finish()
    }
}
