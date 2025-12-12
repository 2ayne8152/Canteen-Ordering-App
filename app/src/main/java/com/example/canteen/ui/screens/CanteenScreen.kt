package com.example.canteen.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.canteen.ui.screens.loginscreens.LoginScreen
import com.example.canteen.ui.screens.loginscreens.StaffLoginScreen
import com.example.canteen.ui.screens.payment.PayByCard
import com.example.canteen.ui.screens.payment.PaymentHistory
import com.example.canteen.ui.screens.payment.PaymentMethod
import com.example.canteen.ui.screens.payment.RefundDetailPage
import com.example.canteen.ui.screens.payment.RefundManagementScreenWrapper
import com.example.canteen.ui.screens.staffMenu.MenuItemForm
import com.example.canteen.ui.screens.staffMenu.MenuListPage
import com.example.canteen.viewmodel.payment.CardDetailViewModel
import com.example.canteen.viewmodel.payment.ReceiptViewModel
import com.example.canteen.viewmodel.payment.RefundViewModel
import com.example.menumanagement.StaffDashboardScreen

enum class CanteenScreen(val title: String) {
    PaymentMethod(title = "PaymentMethod"),
    PayByCard(title = "PayByCard"),
    StaffDashboard(title = "StaffDashboard"),
    MenuItemForm(title = "MenuItemForm"),
    MenuListPage(title = "MenuListPage"),
    PaymentHistory(title = "PaymentHistory"),
    RefundManagementScreenWrapper(title = "RefundManagement"),
    RefundDetailPage(title = "RefundDetail")

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CanteenScreen(
    cardDetailViewModel: CardDetailViewModel = viewModel(),
    receiptViewModel: ReceiptViewModel = viewModel(),
    refundViewModel: RefundViewModel = viewModel()
) {
    val navController = rememberNavController()
    val savedCard by cardDetailViewModel.savedCard.collectAsState()
    var selectedMethod by remember { mutableStateOf<String?>(null) }

    NavHost(navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onStaffLoginClick = { navController.navigate("staff_login") },
                onLoginSuccess = { role ->
                    when (role) {
                        "user" -> navController.navigate("user_menu") {
                            popUpTo("login") { inclusive = true }
                        }
                        "staff" -> navController.navigate(CanteenScreen.StaffDashboard.name) {
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
                        "staff" -> navController.navigate(CanteenScreen.StaffDashboard.name) {
                            popUpTo("staff_login") { inclusive = true }
                        }
                        "user" -> navController.navigate("user_menu") {
                            popUpTo("staff_login") { inclusive = true }
                        }
                    }
                }
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
            MenuListPage(navController)
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
                onBack = {navController.popBackStack()}
            )
        }

        composable(CanteenScreen.PaymentHistory.name){
            PaymentHistory(
                navController = navController,
                receiptViewModel = receiptViewModel
            )
        }

        composable(CanteenScreen.PaymentMethod.name) {
            PaymentMethod(
                cardDetailViewModel = cardDetailViewModel,
                phoneNumber = "0123456789",
                onCardSelected = {
                    navController.navigate(CanteenScreen.PayByCard.name)
                },
                savedCard = savedCard?.maskedCard,
                onMethodSelected = { method ->
                    selectedMethod = method
                    println("Selected method = $method")
                }
            )
        }

        composable(CanteenScreen.PayByCard.name) {
            PayByCard(
                viewModel = cardDetailViewModel,
                onBack = { navController.popBackStack() }
            )
        }

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
        /*composable("user_menu") {
            UserMenu(
                onDetailClick = {
                    // TODO: Navigate to order details/cart screen
                }
            )
        }*/

        // Staff Menu Screen
        composable("staff_menu") {
            StaffDashboardScreen(navController)
        }
    }
}
