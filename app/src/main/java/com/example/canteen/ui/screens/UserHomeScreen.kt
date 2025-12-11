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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.canteen.data.MenuItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHomeScreen(
    menuItems: List<MenuItem>,
    numOfItem: Int,
    totalPrice: Double,
    onAddToCart: (MenuItem, Int) -> Unit,
    onViewCart: () -> Unit,
    onItemClick: (MenuItem) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                onOrderClick = { /* TODO */ },
                onOrderHistoryClick = { /* TODO */ }
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
            UserMenu(
                menuItems = menuItems,
                totalItemsInCart = numOfItem,
                totalPriceInCart = totalPrice,
                onAddToCart = onAddToCart,
                onViewCart = onViewCart,
                onItemClick = onItemClick,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun DrawerContent(
    onOrderClick: () -> Unit,
    onOrderHistoryClick: () -> Unit
) {
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
