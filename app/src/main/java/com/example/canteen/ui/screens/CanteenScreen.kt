package com.example.canteen.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.canteen.data.sampleMenuItems
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
    val menuItems by userMenuViewModel.menuItems.collectAsState()

    LaunchedEffect(role) {
        Log.w("Log", "role")
        if (role == "staff"){
            receiptViewModel.startListeningOnce()
            Log.w("Log", "role = staff")
            Log.w("Log", "${userId}")
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.LoggedOut) {
            receiptViewModel.stopListening()
        }
    }

    LaunchedEffect(userId) {
        if (userId != null) {
            userViewModel.loadUserById(userId)
        }
    }

    NavHost(navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onStaffLoginClick = { navController.navigate("staff_login") },
                onLoginSuccess = { role ->
                    when (role) {
                        "user" -> navController.navigate(CanteenScreen.UserHomeScreen.name) {
                            popUpTo("login") { inclusive = true }
                        }

                        "staff" -> navController.navigate(CanteenScreen.StaffDashboard.name) {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                authViewModel = authViewModel,
                onForgotPasswordClick = { navController.navigate("forgot_password")}
            )
        }

        // Staff Login Screen
        composable("staff_login") {
            StaffLoginScreen(
                onUserLoginClick = { navController.navigate("login") },
                onLoginSuccess = { role ->
                    when (role) {
                        "staff" -> navController.navigate(CanteenScreen.StaffDashboard.name) {
                            popUpTo("staff_login") { inclusive = true }
                        }
                        "user" -> navController.navigate("user_menu") {
                            popUpTo("staff_login") { inclusive = true }
                        }
                    }
                },
                authViewModel = authViewModel,
                onForgotPasswordClick = { navController.navigate("forgot_password")}
            )
        }

        composable("forgot_password") {
            ForgotPasswordScreen(
                authViewModel = authViewModel,
                onBackToLoginClick = {
                    // Go back to login screen
                    navController.popBackStack()
                }
            )
        }

        composable(CanteenScreen.UserHomeScreen.name) {
            UserHomeScreen(
                menuItems = sampleMenuItems,
                onItemClick = {}
            )
        }

        // -------------------- Staff Dashboard --------------------
        composable(CanteenScreen.StaffDashboard.name) {
            StaffDashboardScreen(navController)
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
                receiptViewModel = receiptViewModel,
                refundViewModel = refundViewModel,
                onBack = {navController.popBackStack()},
                userViewModel = userViewModel
            )
        }

        composable(CanteenScreen.PaymentHistory.name){
            PaymentHistory(
                navController = navController,
                receiptViewModel = receiptViewModel
            )
        }
    }
}