package com.example.canteen.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.canteen.data.MenuItem
import com.example.canteen.ui.screens.payment.MakePayment
import com.example.canteen.viewmodel.login.UserViewModel
import com.example.canteen.viewmodel.payment.ReceiptViewModel
import com.example.canteen.viewmodel.usermenu.CartViewModel
import kotlinx.coroutines.launch
import com.example.canteen.viewmodel.AuthViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHomeScreen(
    menuItems: List<MenuItem>,
    onItemClick: (MenuItem) -> Unit = {},
    receiptViewModel: ReceiptViewModel,
    userViewModel: UserViewModel,
    onSignOut: () -> Unit  // Add this parameter
) {
    val cartViewModel: CartViewModel = viewModel()
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val totalItems = cartViewModel.totalItems.collectAsState(initial = 0)
    val totalPrice = cartViewModel.totalPrice.collectAsState(initial = 0.0)

    // track the current route
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route ?: "order"

    // assign screen title dynamically
    val topBarTitle = when (currentRoute) {
        "order" -> "Order"
        "cart" -> "Your Cart"
        "history" -> "Order History"
        "makePayment" -> "Complete your Payment"
        else -> "Canteen"
    }

    val isCartScreen = currentRoute == "cart"
    val isMakePaymentScreen = currentRoute == "makePayment"

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
                },
                onSignOut = {
                    scope.launch { drawerState.close() }
                    // Clear cart and sign out
                    cartViewModel.clearCart()
                    onSignOut()
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(topBarTitle) },
                    navigationIcon = {
                        if (isCartScreen || isMakePaymentScreen) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
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
                            navController.navigate("makePayment")
                        }
                    )
                }

                composable("makePayment") {
                    MakePayment(
                        receiptViewModel = receiptViewModel,
                        userViewModel = userViewModel,
                        onBack = { navController.popBackStack() },
                        onClick = {
                            navController.navigate("order") {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                        cartViewModel = cartViewModel
                    )
                }

                composable("history") {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Order History (placeholder)", fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerContent(
    onOrderClick: () -> Unit,
    onOrderHistoryClick: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = 48.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top section - Menu items
        Column {
            DrawerItem(Icons.Default.ShoppingCart, "Order", onOrderClick)
            DrawerItem(Icons.Default.History, "Order History", onOrderHistoryClick)
        }

        // Bottom section - Logout
        Column {
            Divider(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
            DrawerItem(Icons.AutoMirrored.Filled.Logout, "Logout", onSignOut)
        }
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
        Icon(
            icon,
            contentDescription = text,
            tint = if (text == "Logout") MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 18.sp,
            color = if (text == "Logout") MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface
        )
    }
}