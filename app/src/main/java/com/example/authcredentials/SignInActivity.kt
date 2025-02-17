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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class SignInActivity : ComponentActivity() {
    private lateinit var viewModel: GoogleSignInViewModel  // Using ViewModel for better handling

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(GoogleSignInViewModel::class.java)

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
            viewModel.handleGoogleSignIn(this@SignInActivity) { success ->
                if (success) {
                    navigateToHome()
                } else {
                    navigateToFailure()
                }
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
