package com.example.canteen.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.canteen.ui.theme.AppColors
import com.example.canteen.viewmodel.login.UserViewModel
import com.example.canteen.viewmodel.payment.ReceiptViewModel
import com.example.canteen.viewmodel.usermenu.CartViewModel
import kotlinx.coroutines.launch
import com.example.canteen.viewmodel.AuthViewModel
import com.example.canteen.viewmodel.usermenu.OrderViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHomeScreen(
    menuItems: List<MenuItem>,
    onItemClick: (MenuItem) -> Unit = {},
    receiptViewModel: ReceiptViewModel,
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModel,
    userViewModel: UserViewModel,
    onSignOut: () -> Unit
) {
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
        "orderDetail" -> "Order Details"
        else -> "Canteen"
    }

    val isCartScreen = currentRoute == "cart"
    val isMakePaymentScreen = currentRoute == "makePayment"
    val isOrderDetailScreen = currentRoute == "orderDetail"

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
            containerColor = AppColors.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            topBarTitle,
                            color = AppColors.textPrimary,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        if (isCartScreen || isMakePaymentScreen || isOrderDetailScreen) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = AppColors.textPrimary
                                )
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = AppColors.textPrimary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = AppColors.surface,
                        titleContentColor = AppColors.textPrimary,
                        navigationIconContentColor = AppColors.textPrimary
                    )
                )
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "order",
                modifier = Modifier.padding(top = padding.calculateTopPadding())
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
                        cartViewModel = cartViewModel,
                        orderViewModel = orderViewModel
                    )
                }

                composable("history") {
                    OrderHistoryScreen(
                        userViewModel = userViewModel,
                        orderViewModel = orderViewModel,
                        onOrderClick = { order ->
                            orderViewModel.selectOrder(order)
                            navController.navigate("orderDetail")
                        }
                    )
                }
                composable("orderDetail") {
                    val selectedOrder by orderViewModel.selectedOrder.collectAsState()

                    selectedOrder?.let { order ->
                        OrderDetailScreen(
                            order = order,
                            onBack = { navController.popBackStack() }
                        )
                    } ?: Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppColors.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Order not found",
                            color = AppColors.textPrimary
                        )
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
            .width(280.dp)
            .background(AppColors.surface)
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
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                thickness = 1.dp,
                color = AppColors.divider
            )
            DrawerItem(Icons.AutoMirrored.Filled.Logout, "Logout", onSignOut, isLogout = true)
        }
    }
}

@Composable
fun DrawerItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    isLogout: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = text,
            tint = if (isLogout) AppColors.error else AppColors.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            color = if (isLogout) AppColors.error else AppColors.textPrimary,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}