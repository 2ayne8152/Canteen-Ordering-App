package com.example.canteen.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.canteen.data.Order
import com.example.canteen.viewmodel.login.UserViewModel
import com.example.canteen.viewmodel.usermenu.OrderViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel
) {
    val user by userViewModel.selectedUser.collectAsState()

    val userId = user?.UserID?.trim()

    val orders by orderViewModel.orderHistory.collectAsState()

    LaunchedEffect(userId) {
        orderViewModel.startListeningOrderHistory(userId!!)
    }

    DisposableEffect(Unit) {
        onDispose {
            orderViewModel.stopListeningOrderHistory()
        }
    }

    Scaffold()
    { padding ->
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No orders found")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                items(orders) { order ->
                    OrderHistoryItem(order)
                }
            }
        }
    }
}

@Composable
fun OrderHistoryItem(order: Order) {

    val date = remember(order.createdAt) {
        SimpleDateFormat(
            "dd MMM yyyy, HH:mm",
            Locale.getDefault()
        ).format(order.createdAt.toDate())
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Order ID: ${order.orderId}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Total: RM ${order.totalAmount}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Status: ${order.status}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("Date: $date", style = MaterialTheme.typography.bodySmall)
        }
    }
}
