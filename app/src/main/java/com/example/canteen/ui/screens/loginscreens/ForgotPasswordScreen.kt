
package com.example.canteen.ui.screens.loginscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.canteen.ui.theme.AppColors
import com.example.canteen.viewmodel.AuthViewModel
@Composable
fun ForgotPasswordScreen(
    authViewModel: AuthViewModel,
    onBackToLoginClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val passwordResetStatus by authViewModel.passwordResetStatus.collectAsState()
    val isLoading by authViewModel.isLoadingPasswordReset.collectAsState()

    passwordResetStatus?.let {
        AlertDialog(
            onDismissRequest = { authViewModel.clearPasswordResetStatus() },
            title = {
                Text(
                    if (it.startsWith("Failed")) "Error" else "Success",
                    color = if (it.startsWith("Failed")) AppColors.error else AppColors.success
                )
            },
            text = { Text(it, color = AppColors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.clearPasswordResetStatus()
                        if (!it.startsWith("Failed")) {
                            onBackToLoginClick()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (it.startsWith("Failed")) AppColors.error else AppColors.success
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
            // Icon
            Surface(
                color = AppColors.warning,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "🔑",
                        fontSize = 40.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.headlineLarge,
                color = AppColors.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            )

            Text(
                text = "Enter your email to receive reset link",
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
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address", color = AppColors.textSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.warning,
                        unfocusedBorderColor = AppColors.divider,
                        focusedTextColor = AppColors.textPrimary,
                        unfocusedTextColor = AppColors.textPrimary,
                        cursorColor = AppColors.warning
                    ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { authViewModel.sendPasswordResetEmail(email) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading && email.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.warning,
                        disabledContainerColor = AppColors.disabled
                    ),
                    shape = RoundedCornerShape(50.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = AppColors.background
                        )
                    } else {
                        Text(
                            "Send Reset Link",
                            color = AppColors.background,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Back to login
            Surface(
                color = AppColors.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .clickable { onBackToLoginClick() }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "← Back to Login",
                    color = AppColors.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}