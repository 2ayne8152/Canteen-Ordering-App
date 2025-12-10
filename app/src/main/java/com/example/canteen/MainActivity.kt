package com.example.canteen

import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.example.canteen.ui.screens.CanteenScreen
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
import com.example.canteen.ui.screens.staffMenu.MenuItemForm
import com.example.canteen.ui.screens.staffMenu.MenuListPage
import com.example.canteen.ui.screens.staffMenu.MenuListPagePreview
import com.example.canteen.ui.theme.CanteenTheme
import com.example.menumanagement.StaffDashboardScreen

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CanteenTheme {
                CanteenScreen()
            }
        }

        /*Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            CanteenApp()
        }*/

    }
}

@Composable
fun CanteenApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        // User Login Screen
        composable("login") {
            LoginScreen(
                onStaffLoginClick = { navController.navigate("staff_login") },
                onLoginSuccess = { role ->
                    when (role) {
                        "user" -> navController.navigate("user_menu") {
                            popUpTo("login") { inclusive = true }
                        }
                        "staff" -> navController.navigate("staff_menu") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            )
        }

        // Staff Login Screen
        composable("staff_login") {
            StaffLoginScreen(
                onUserLoginClick = { navController.navigate("login") },
                onLoginSuccess = { role ->
                    when (role) {
                        "staff" -> navController.navigate("staff_menu") {
                            popUpTo("staff_login") { inclusive = true }
                        }
                        "user" -> navController.navigate("user_menu") {
                            popUpTo("staff_login") { inclusive = true }
                        }
                    }
                }
            )
        }

        // User Menu Screen
        composable("user_menu") {
            UserMenu(
                onDetailClick = {
                    // TODO: Navigate to order details/cart screen
                }
            )
        }

    }
}

