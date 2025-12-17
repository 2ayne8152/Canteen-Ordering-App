package com.example.canteen.ui.screens

import android.os.Build
import android.text.Layout
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.canteen.ui.screens.loginscreens.ForgotPasswordScreen
import com.example.canteen.ui.screens.loginscreens.LoginScreen
import com.example.canteen.ui.screens.loginscreens.StaffLoginScreen
import com.example.canteen.ui.screens.payment.PaymentHistory
import com.example.canteen.ui.screens.payment.RefundDetailPage
import com.example.canteen.ui.screens.payment.RefundManagementScreenWrapper
import com.example.canteen.ui.screens.staffMenu.MenuItemForm
import com.example.canteen.ui.screens.staffMenu.StaffMenuListPage
import com.example.canteen.viewmodel.AuthState
import com.example.canteen.viewmodel.AuthViewModel
import com.example.canteen.viewmodel.login.UserViewModel
import com.example.canteen.viewmodel.payment.CardDetailViewModel
import com.example.canteen.viewmodel.payment.ReceiptViewModel
import com.example.canteen.viewmodel.payment.RefundViewModel
import com.example.canteen.viewmodel.usermenu.CartViewModel
import com.example.canteen.viewmodel.usermenu.UserMenuViewModel
import com.example.canteen.viewmodel.usermenu.order.OrderViewModel
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
    cartViewModel: CartViewModel = viewModel(),
    orderViewModel: OrderViewModel = viewModel(),
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

    // Auto-navigate based on auth state
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Initial -> {
                // Still checking, do nothing
                Log.d("CanteenScreen", "Checking for existing session...")
            }
            is AuthState.LoggedIn -> {
                val currentLoggedInState = authState as AuthState.LoggedIn  // Different variable name
                Log.d("CanteenScreen", "User logged in with role: ${currentLoggedInState.role}")
                // Navigate to appropriate screen based on role
                when (currentLoggedInState.role) {
                    "user" -> {
                        navController.navigate(CanteenScreen.UserHomeScreen.name) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                    "staff" -> {
                        navController.navigate(CanteenScreen.StaffDashboard.name) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                }
            }
            is AuthState.LoggedOut -> {
                // Navigate to login when logged out
                Log.d("CanteenScreen", "User logged out, navigating to login")
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
            else -> {
                // For other states, stay on current screen
            }
        }
    }

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
                cartViewModel = cartViewModel,
                orderViewModel = orderViewModel,
                userViewModel = userViewModel,
                onSignOut = { authViewModel.signOut() }
            )
        }

        // -------------------- STAFF DASHBOARD --------------------
        composable(CanteenScreen.StaffDashboard.name) {
            StaffDashboardScreen(navController, onClick = { authViewModel.signOut() })
        }

        composable(CanteenScreen.MenuItemForm.name){
            MenuItemForm(navController)
        }

        composable (CanteenScreen.MenuListPage.name){
            StaffMenuListPage(navController)
        }

        composable (CanteenScreen.RefundManagementScreenWrapper.name){
            RefundManagementScreenWrapper (
                receiptViewModel = receiptViewModel,
                navController = navController,
                onClick = {navController.navigate(CanteenScreen.RefundDetailPage.name)}
            )
        }

        composable(CanteenScreen.RefundDetailPage.name){
            RefundDetailPage(
                orderViewModel = orderViewModel,
                receiptViewModel = receiptViewModel,
                refundViewModel = refundViewModel,
                onBack = {navController.popBackStack()},
                userViewModel = userViewModel
            )
        }

        composable(CanteenScreen.PaymentHistory.name){
            PaymentHistory(
                navController = navController,
                receiptViewModel = receiptViewModel,
                orderViewModel = orderViewModel
            )
        }
    }
}

