package com.example.canteen.ui.screens.loginscreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.canteen.ui.theme.CanteenTheme
import com.example.canteen.viewmodel.AuthViewModel
import com.example.canteen.viewmodel.AuthState

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onStaffLoginClick: () -> Unit,
    onLoginSuccess: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isRegistering by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()

    // Track if we're currently registering to prevent false login triggers
    val isCurrentlyRegistering = remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.LoggedIn -> {
                // Only navigate if we're NOT in the middle of registration
                if (!isCurrentlyRegistering.value) {
                    onLoginSuccess((authState as AuthState.LoggedIn).role)
                } else {
                    // If we get LoggedIn during registration, reset and show error
                    authViewModel.resetAuthState()
                }
            }
            // Don't auto-navigate on registration success - let user click OK first
            else -> {
                // Reset registration flag when not loading
                if (authState !is AuthState.Loading) {
                    isCurrentlyRegistering.value = false
                }
            }
        }
    }

    // Show success dialog
    if (authState is AuthState.RegistrationSuccess) {
        AlertDialog(
            onDismissRequest = {
                authViewModel.resetAuthState()
                isCurrentlyRegistering.value = false
            },
            title = { Text("Registration Successful!") },
            text = {
                Text((authState as AuthState.RegistrationSuccess).message)
            },
            confirmButton = {
                Button(onClick = {
                    authViewModel.resetAuthState()
                    isRegistering = false
                    isCurrentlyRegistering.value = false
                    email = ""
                    password = ""
                    username = ""
                }) {
                    Text("OK")
                }
            }
        )
    }

    // Show error dialog
    if (authState is AuthState.Error) {
        AlertDialog(
            onDismissRequest = {
                authViewModel.resetAuthState()
                isCurrentlyRegistering.value = false
            },
            title = { Text("Error") },
            text = {
                Text((authState as AuthState.Error).message)
            },
            confirmButton = {
                Button(onClick = {
                    authViewModel.resetAuthState()
                    isCurrentlyRegistering.value = false
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
                text = if (isRegistering) "Register" else "Canteen Login",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Registration form fields
                if (isRegistering) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = authState !is AuthState.Loading
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = authState !is AuthState.Loading
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) "Hide password" else "Show password"
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            enabled = authState !is AuthState.Loading
                        ) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = authState !is AuthState.Loading
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Show loading text on button if loading, otherwise show normal button text
                val buttonText = if (authState is AuthState.Loading) {
                    if (isRegistering) "Registering..." else "Logging in..."
                } else {
                    if (isRegistering) "Register" else "Login"
                }

                Button(
                    onClick = {
                        if (isRegistering) {
                            // Set flag that we're registering
                            isCurrentlyRegistering.value = true
                            authViewModel.register(email, password, username, "user")
                        } else {
                            isCurrentlyRegistering.value = false
                            authViewModel.login(email, password, "user")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = authState !is AuthState.Loading &&
                            email.isNotEmpty() &&
                            password.isNotEmpty() &&
                            (!isRegistering || username.isNotEmpty())
                ) {
                    Text(buttonText)
                }

                // Emergency reset button (visible only when stuck in loading)
                if (authState is AuthState.Loading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = {
                            // Force reset everything
                            authViewModel.resetAuthState()
                            isCurrentlyRegistering.value = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset (if stuck)")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isRegistering) "Already have an account? Login" else "Don't have an account? Register",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        if (authState !is AuthState.Loading) {
                            isRegistering = !isRegistering
                            isCurrentlyRegistering.value = false
                            authViewModel.resetAuthState()
                            username = ""
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Staff access",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        if (authState !is AuthState.Loading) {
                            onStaffLoginClick()
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    CanteenTheme {
        LoginScreen(onStaffLoginClick = {}, onLoginSuccess = {})
    }
}