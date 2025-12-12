package com.example.canteen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.canteen.data.MenuItem
import com.example.canteen.viewmodel.usermenu.CartViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHomeScreen(
    menuItems: List<MenuItem>,
    // pass no. and total for initial preview (live values will come from CartViewModel below)
    onItemClick: (MenuItem) -> Unit = {},
) {
    val cartViewModel: CartViewModel = viewModel()
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // collect totals for the floating bar
    val totalItems = cartViewModel.totalItems.collectAsState(initial = 0)
    val totalPrice = cartViewModel.totalPrice.collectAsState(initial = 0.0)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                onOrderClick = {
                    navController.navigate("order") { launchSingleTop = true }
                    scope.launch { drawerState.close() }
                },
                onOrderHistoryClick = {
                    navController.navigate("history") { launchSingleTop = true }
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Canteen") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "order",
                modifier = Modifier.padding(padding)
            ) {
                composable("order") {
                    UserMenu(
                        menuItems = menuItems,
                        totalItemsInCart = totalItems.value,
                        totalPriceInCart = totalPrice.value,
                        onAddToCart = { item, qty -> cartViewModel.addToCart(item, qty) },
                        onViewCart = { navController.navigate("cart") },
                        onItemClick = onItemClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                composable("cart") {
                    CartScreen(
                        cartViewModel = cartViewModel,
                        onBack = { navController.popBackStack() },
                        onCheckout = {
                            // TODO: implement checkout flow (save order to cloud)
                            cartViewModel.clearCart()
                            navController.popBackStack()
                        }
                    )
                }

                composable("history") {
                    // placeholder
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Order History (placeholder)", fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerContent(onOrderClick: () -> Unit, onOrderHistoryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = 48.dp)
    ) {
        DrawerItem(Icons.Default.ShoppingCart, "Order", onOrderClick)
        DrawerItem(Icons.Default.History, "Order History", onOrderHistoryClick)
    }
}

@Composable
fun DrawerItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = text, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, fontSize = 18.sp)
    }
}
