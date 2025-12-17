package com.example.canteen.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.canteen.data.Order
import com.example.canteen.viewmodel.login.UserViewModel
import com.example.canteen.viewmodel.usermenu.OrderViewModel
import java.text.SimpleDateFormat
import java.util.*

private enum class OrderTab(val title: String) {
    PREPARING("Preparing"),
    COMPLETED("Completed"),
    REFUNDED("Refunded")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel,
    onOrderClick: (Order) -> Unit
) {
    val user by userViewModel.selectedUser.collectAsState()
    val userId = user?.UserID?.trim()

    val orders by orderViewModel.orderHistory.collectAsState()
    var selectedTab by remember { mutableStateOf(OrderTab.PREPARING) }

    LaunchedEffect(userId) {
        userId?.let {
            orderViewModel.startListeningOrderHistory(it)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            orderViewModel.stopListeningOrderHistory()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ===== Tabs below TopBar =====
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                OrderTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.title) }
                    )
                }
            }

            val filteredOrders = remember(orders, selectedTab) {
                when (selectedTab) {
                    OrderTab.PREPARING ->
                        orders.filter {
                            it.status.equals("PENDING", true) ||
                                    it.status.equals("READY TO PICKUP", true)
                        }

                    OrderTab.COMPLETED ->
                        orders.filter {
                            it.status.equals("COMPLETED", true)
                        }

                    OrderTab.REFUNDED ->
                        orders.filter {
                            it.status.equals("REFUNDED", true)
                        }
                }
            }

            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (selectedTab) {
                            OrderTab.PREPARING -> "No orders being prepared"
                            OrderTab.COMPLETED -> "No completed orders"
                            OrderTab.REFUNDED -> "No refunded orders"
                        }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredOrders) { order ->
                        OrderHistoryItem(
                            order = order,
                            onOrderClick = onOrderClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderHistoryItem(
    order: Order,
    onOrderClick: (Order) -> Unit
) {
    val date = remember(order.createdAt) {
        SimpleDateFormat(
            "dd MMM yyyy, HH:mm",
            Locale.getDefault()
        ).format(order.createdAt.toDate())
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOrderClick(order) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = "Order #${order.orderId.takeLast(6)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Date: $date",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Status: ${order.status}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "RM ${"%.2f".format(order.totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
