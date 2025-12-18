package com.example.canteen.ui.screens.loginscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.canteen.ui.theme.AppColors
import com.example.canteen.viewmodel.AuthViewModel
import com.example.canteen.viewmodel.AuthState
@Composable
fun StaffLoginScreen(
    authViewModel: AuthViewModel,
    onUserLoginClick: () -> Unit,
    onLoginSuccess: (String) -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isRegistering by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()
    val isCurrentlyRegistering = remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.LoggedIn -> {
                if (!isCurrentlyRegistering.value) {
                    onLoginSuccess((authState as AuthState.LoggedIn).role)
                } else {
                    authViewModel.resetAuthState()
                }
            }
            else -> {
                if (authState !is AuthState.Loading) {
                    isCurrentlyRegistering.value = false
                }
            }
        }
    }

    if (authState is AuthState.RegistrationSuccess) {
        AlertDialog(
            onDismissRequest = {
                isCurrentlyRegistering.value = false
            },
            title = { Text("Registration Successful!", color = AppColors.textPrimary) },
            text = {
                Text(
                    (authState as AuthState.RegistrationSuccess).message,
                    color = AppColors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.signOut()
                        isRegistering = false
                        isCurrentlyRegistering.value = false
                        email = ""
                        password = ""
                        username = ""
                        phoneNumber = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("OK", color = AppColors.surface)
                }
            },
            containerColor = AppColors.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (authState is AuthState.Error) {
        AlertDialog(
            onDismissRequest = {
                authViewModel.clearError()
                isCurrentlyRegistering.value = false
            },
            title = { Text("Error", color = AppColors.error) },
            text = {
                Text(
                    (authState as AuthState.Error).message,
                    color = AppColors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.clearError()
                        isCurrentlyRegistering.value = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.error
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("OK", color = AppColors.surface)
                }
            },
            containerColor = AppColors.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo/Brand Section
            Surface(
                color = AppColors.info,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "👨‍🍳",
                        fontSize = 40.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isRegistering) "Staff Registration" else "Staff Portal",
                style = MaterialTheme.typography.headlineLarge,
                color = AppColors.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            )

            Text(
                text = if (isRegistering) "Create staff account" else "Staff access only",
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.textSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Form inputs
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isRegistering) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username", color = AppColors.textSecondary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = authState !is AuthState.Loading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.info,
                            unfocusedBorderColor = AppColors.divider,
                            focusedTextColor = AppColors.textPrimary,
                            unfocusedTextColor = AppColors.textPrimary,
                            cursorColor = AppColors.info
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number", color = AppColors.textSecondary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = authState !is AuthState.Loading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.info,
                            unfocusedBorderColor = AppColors.divider,
                            focusedTextColor = AppColors.textPrimary,
                            unfocusedTextColor = AppColors.textPrimary,
                            cursorColor = AppColors.info
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = AppColors.textSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = authState !is AuthState.Loading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.info,
                        unfocusedBorderColor = AppColors.divider,
                        focusedTextColor = AppColors.textPrimary,
                        unfocusedTextColor = AppColors.textPrimary,
                        cursorColor = AppColors.info
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = AppColors.textSecondary) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            enabled = authState !is AuthState.Loading
                        ) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = AppColors.textSecondary
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = authState !is AuthState.Loading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.info,
                        unfocusedBorderColor = AppColors.divider,
                        focusedTextColor = AppColors.textPrimary,
                        unfocusedTextColor = AppColors.textPrimary,
                        cursorColor = AppColors.info
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                if (!isRegistering) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Forgot Password?",
                        color = AppColors.info,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable { onForgotPasswordClick() }
                            .align(Alignment.End)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (isRegistering) {
                            isCurrentlyRegistering.value = true
                            authViewModel.register(email, password, username, "staff", phoneNumber)
                        } else {
                            isCurrentlyRegistering.value = false
                            authViewModel.login(email, password, "staff")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = authState !is AuthState.Loading &&
                            email.isNotEmpty() &&
                            password.isNotEmpty() &&
                            (!isRegistering || username.isNotEmpty()),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.info,
                        disabledContainerColor = AppColors.disabled
                    ),
                    shape = RoundedCornerShape(50.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = AppColors.surface
                        )
                    } else {
                        Text(
                            if (isRegistering) "Register Staff" else "Staff Sign In",
                            color = AppColors.surface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (authState is AuthState.Loading) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            authViewModel.resetAuthState()
                            isCurrentlyRegistering.value = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AppColors.textSecondary
                        )
                    ) {
                        Text("Reset (if stuck)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Toggle between login/register
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isRegistering) "Already have a staff account? " else "Don't have a staff account? ",
                    color = AppColors.textSecondary
                )
                Text(
                    text = if (isRegistering) "Sign In" else "Register",
                    color = AppColors.info,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        if (authState !is AuthState.Loading) {
                            isRegistering = !isRegistering
                            isCurrentlyRegistering.value = false
                            username = ""
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(
                color = AppColors.divider,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // User access
            Surface(
                color = AppColors.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .clickable {
                        if (authState !is AuthState.Loading) {
                            onUserLoginClick()
                        }
                    }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "User Access →",
                    color = AppColors.info,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}