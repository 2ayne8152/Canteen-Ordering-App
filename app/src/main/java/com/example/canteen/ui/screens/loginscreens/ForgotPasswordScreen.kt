package com.example.canteen.ui.screens.loginscreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.canteen.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    authViewModel: AuthViewModel = viewModel(),
    onBackToLoginClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val passwordResetStatus by authViewModel.passwordResetStatus.collectAsState()
    val isLoading by authViewModel.isLoadingPasswordReset.collectAsState()

    // Show status dialog
    passwordResetStatus?.let {
        AlertDialog(
            onDismissRequest = { authViewModel.clearPasswordResetStatus() },
            title = { Text(if (it.startsWith("Failed")) "Error" else "Success") },
            text = { Text(it) },
            confirmButton = {
                Button(onClick = {
                    authViewModel.clearPasswordResetStatus()
                    // If successful, navigate back
                    if (!it.startsWith("Failed")) {
                        onBackToLoginClick()
                    }
                }) {
                    Text("OK")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Enter your email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { authViewModel.sendPasswordResetEmail(email) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading && email.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Send Reset Link")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Back to Login",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onBackToLoginClick() }
            )
        }
    }
}