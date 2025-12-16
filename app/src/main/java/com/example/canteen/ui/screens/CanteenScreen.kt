package com.example.canteen.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.canteen.ui.screens.loginscreens.ForgotPasswordScreen
import com.example.canteen.ui.screens.loginscreens.LoginScreen
import com.example.canteen.ui.screens.loginscreens.StaffLoginScreen
import com.example.canteen.viewmodel.AuthState
import com.example.canteen.viewmodel.AuthViewModel
import com.example.canteen.viewmodel.login.UserViewModel
import com.example.canteen.viewmodel.payment.CardDetailViewModel
import com.example.canteen.viewmodel.payment.ReceiptViewModel
import com.example.canteen.viewmodel.payment.RefundViewModel
import com.example.canteen.viewmodel.usermenu.UserMenuViewModel
import com.example.menumanagement.StaffDashboardScreen

enum class CanteenScreen(val title: String) {
    StaffDashboard(title = "StaffDashboard"),
    MenuItemForm(title = "MenuItemForm"),
    MenuListPage(title = "MenuListPage"),
    PaymentHistory(title = "PaymentHistory"),
    RefundManagementScreenWrapper(title = "RefundManagement"),
    RefundDetailPage(title = "RefundDetail"),
    MakePayment(title = "MakePayment"),
    UserHomeScreen(title = "UserHomeScreen")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CanteenScreen(
    cardDetailViewModel: CardDetailViewModel = viewModel(),
    receiptViewModel: ReceiptViewModel = viewModel(),
    refundViewModel: RefundViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    userMenuViewModel: UserMenuViewModel = viewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()
    val userId = (authState as? AuthState.LoggedIn)?.userId
    val role = (authState as? AuthState.LoggedIn)?.role

    // Firestore menu items
    val menuItems by userMenuViewModel.menuItems.collectAsState()

    LaunchedEffect(role) {
        if (role == "staff") {
            receiptViewModel.startListeningOnce()
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.LoggedOut) {
            receiptViewModel.stopListening()
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    LaunchedEffect(userId) {
        if (userId != null) {
            userViewModel.loadUserById(userId)
        }
    }

    NavHost(navController, startDestination = "login") {

        // -------------------- LOGIN --------------------
        composable("login") {
            LoginScreen(
                onStaffLoginClick = { navController.navigate("staff_login") },
                onLoginSuccess = { userRole ->
                    when (userRole) {
                        "user" -> navController.navigate(CanteenScreen.UserHomeScreen.name) {
                            popUpTo("login") { inclusive = true }
                        }
                        "staff" -> navController.navigate(CanteenScreen.StaffDashboard.name) {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                authViewModel = authViewModel,
                onForgotPasswordClick = { navController.navigate("forgot_password") }
            )
        }

        // -------------------- STAFF LOGIN --------------------
        composable("staff_login") {
            StaffLoginScreen(
                onUserLoginClick = { navController.navigate("login") },
                onLoginSuccess = { userRole ->
                    when (userRole) {
                        "staff" -> navController.navigate(CanteenScreen.StaffDashboard.name) {
                            popUpTo("staff_login") { inclusive = true }
                        }
                        "user" -> navController.navigate(CanteenScreen.UserHomeScreen.name) {
                            popUpTo("staff_login") { inclusive = true }
                        }
                    }
                },
                authViewModel = authViewModel,
                onForgotPasswordClick = { navController.navigate("forgot_password") }
            )
        }

        // -------------------- FORGOT PASSWORD --------------------
        composable("forgot_password") {
            ForgotPasswordScreen(
                authViewModel = authViewModel,
                onBackToLoginClick = { navController.popBackStack() }
            )
        }

        // -------------------- USER HOME --------------------
        composable(CanteenScreen.UserHomeScreen.name) {
            UserHomeScreen(
                menuItems = menuItems,
                onItemClick = {},
                receiptViewModel = receiptViewModel,  
                userViewModel = userViewModel,
                onSignOut = {
                    authViewModel.signOut()

                }
            )
        }

        // -------------------- STAFF DASHBOARD --------------------
        composable(CanteenScreen.StaffDashboard.name) {
            StaffDashboardScreen(navController)
        }
    }
}