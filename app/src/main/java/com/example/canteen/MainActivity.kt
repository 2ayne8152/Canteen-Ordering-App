package com.example.canteen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.canteen.ui.theme.CanteenTheme
import com.example.medipoint.ui.theme.Viewmodels.AuthViewModel
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CanteenTheme {
                // Main app navigation logic
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel = viewModel()) {
    val navController = rememberNavController()

    // Observe the login state from the ViewModel
    val isLoggedIn = authViewModel.isLoggedIn.collectAsState()

    // Define the start destination based on login state
    val startDestination = if (isLoggedIn.value) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        // Login Screen Route
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    // Navigate to home and clear the back stack
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegistration = {
                    // You can navigate to a registration screen here if you have one
                    // For now, it does nothing
                }
            )
        }

        // Home Screen Route (Placeholder)
        composable("home") {
            HomeScreen()
        }
    }
}

// A simple placeholder for your home screen after login
@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Welcome! You are logged in.")
    }
}
