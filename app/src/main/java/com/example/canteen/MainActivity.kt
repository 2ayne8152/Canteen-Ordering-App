package com.example.canteen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.canteen.data.MenuItem
import com.example.canteen.ui.screens.UserHomeScreen
import com.example.canteen.viewmodel.usermenu.CartViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val cartViewModel: CartViewModel = viewModel()
            val navController = rememberNavController()

            // Collect totals from ViewModel (because they are Flows)
            val totalItems by cartViewModel.totalItems.collectAsState(initial = 0)
            val totalPrice by cartViewModel.totalPrice.collectAsState(initial = 0.0)

            // Sample static menu (works fine)
            val sampleMenu = listOf(
                MenuItem(
                    id = "1",
                    name = "Cheeseburger",
                    description = "Juicy beef patty with cheese",
                    price = 8.50,
                    remainQuantity = 10,
                    imageUrl = ""
                ),
                MenuItem(
                    id = "2",
                    name = "Veggie Wrap",
                    description = "Healthy veggie wrap with hummus",
                    price = 6.00,
                    remainQuantity = 5,
                    imageUrl = ""
                ),
                MenuItem(
                    id = "3",
                    name = "French Fries",
                    description = "Crispy golden fries",
                    price = 3.50,
                    remainQuantity = 20,
                    imageUrl = ""
                )
            )

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            UserHomeScreen(
                                menuItems = sampleMenu,
                                numOfItem = totalItems,
                                totalPrice = totalPrice,
                                onAddToCart = { item, qty ->
                                    cartViewModel.addToCart(item, qty)
                                },
                                onViewCart = {
                                    navController.navigate("cart")
                                },
                                onItemClick = {}
                            )
                        }

                        // You can create CartScreen later
                        composable("cart") {
                            // TODO: Add CartScreen here
                        }
                    }
                }
            }
        }
    }
}
