package com.example.canteen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.canteen.ui.screens.loginscreens.LoginScreen
import com.example.canteen.ui.screens.loginscreens.StaffLoginScreen
import com.example.canteen.ui.screens.usermenu.UserMenu
import com.example.canteen.ui.theme.CanteenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CanteenTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CanteenApp()
                }
            }
        }
    }
}

@Composable
fun CanteenApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onStaffLoginClick = { navController.navigate("staff_login") },
                onLoginSuccess = {
                    navController.navigate(it) { 
                        popUpTo("login") { inclusive = true } 
                    }
                }
            )
        }
        composable("staff_login") {
            StaffLoginScreen(
                onUserLoginClick = { navController.navigate("login") },
                onLoginSuccess = { role ->
                    when (role) {
                        "staff" -> navController.navigate("staff") {
                            popUpTo("staff_login") { inclusive = true }
                        }
                        // Staff can also login as user if they have that role
                        "user" -> navController.navigate("user") {
                            popUpTo("staff_login") { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("user") {
            UserMenu(onDetailClick = { /* TODO: Navigate to cart/detail screen */ })
        }
        composable("staff") {
            StaffMenu()
        }
    }
}

@Composable
fun StaffMenu() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Staff Menu")
    }
}
