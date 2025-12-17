package com.example.canteen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.canteen.data.Order
import com.example.canteen.ui.theme.AppColors
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

    Scaffold(
        containerColor = AppColors.background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // ===== Tabs below TopBar =====
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = AppColors.surface,
                contentColor = AppColors.textPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                        color = AppColors.primary,
                        height = 3.dp
                    )
                }
            ) {
                OrderTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                tab.title,
                                color = if (selectedTab == tab) AppColors.primary else AppColors.textSecondary,
                                fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 15.sp
                            )
                        },
                        selectedContentColor = AppColors.primary,
                        unselectedContentColor = AppColors.textSecondary
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
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = AppColors.textSecondary.copy(alpha = 0.5f)
                        )
                        Text(
                            text = when (selectedTab) {
                                OrderTab.PREPARING -> "No orders being prepared"
                                OrderTab.COMPLETED -> "No completed orders"
                                OrderTab.REFUNDED -> "No refunded orders"
                            },
                            color = AppColors.textSecondary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.background)
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

    val statusColor = when (order.status.uppercase()) {
        "PENDING" -> AppColors.warning
        "READY TO PICKUP" -> AppColors.info
        "COMPLETED" -> AppColors.success
        "REFUNDED" -> AppColors.error
        else -> AppColors.textSecondary
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOrderClick(order) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AppColors.primary.copy(alpha = 0.15f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = AppColors.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = "Order #${order.orderId.takeLast(6)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.textPrimary,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = order.status.uppercase(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "RM ${"%.2f".format(order.totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.primary,
                    fontSize = 18.sp
                )
            }
        }
    }
}