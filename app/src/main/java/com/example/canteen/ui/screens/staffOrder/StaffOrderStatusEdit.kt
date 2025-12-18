package com.example.canteen.ui.screens.staffOrder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.canteen.R
import com.example.canteen.data.Order
import com.example.canteen.ui.theme.AppColors
import com.example.canteen.viewmodel.staffMenu.Base64Utils
import com.example.canteen.viewmodel.usermenu.OrderViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffOrderStatusEdit(
    navController: NavController,
    orderViewModel: OrderViewModel = viewModel()
) {
    val allOrders by orderViewModel.allOrders.collectAsState()
    val error by orderViewModel.error.collectAsState()

    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var expandedOrderId by remember { mutableStateOf<String?>(null) }

    val statusFilters = listOf("ALL", "PENDING", "READY TO PICKUP", "COMPLETED", "REFUNDED")

    val filteredOrders = remember(allOrders, selectedStatusFilter) {
        if (selectedStatusFilter == "ALL") {
            allOrders
        } else {
            allOrders.filter { it.status.equals(selectedStatusFilter, ignoreCase = true) }
        }
    }

    LaunchedEffect(Unit) {
        orderViewModel.startListeningAllOrders()
    }

    DisposableEffect(Unit) {
        onDispose {
            orderViewModel.stopListeningAllOrders()
        }
    }

    Scaffold(
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Manage Orders",
                        color = AppColors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = AppColors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.surface,
                    titleContentColor = AppColors.textPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppColors.background)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statusFilters.forEach { status ->
                    FilterChip(
                        selected = selectedStatusFilter == status,
                        onClick = { selectedStatusFilter = status },
                        label = {
                            Text(
                                status,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.primary,
                            selectedLabelColor = AppColors.surface,
                            containerColor = AppColors.surface,
                            labelColor = AppColors.textPrimary
                        )
                    )
                }
            }

            if (error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.error.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = "Error: $error",
                        color = AppColors.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Text(
                text = "${filteredOrders.size} order(s) found",
                fontSize = 13.sp,
                color = AppColors.textSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = AppColors.textSecondary.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "No orders found",
                            color = AppColors.textSecondary,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredOrders, key = { it.orderId }) { order ->
                        StaffOrderCard(
                            order = order,
                            isExpanded = expandedOrderId == order.orderId,
                            onExpandClick = {
                                expandedOrderId = if (expandedOrderId == order.orderId) {
                                    null
                                } else {
                                    order.orderId
                                }
                            },
                            onStatusChange = { newStatus ->
                                orderViewModel.orderStatusUpdate(order.orderId, newStatus)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffOrderCard(
    order: Order,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    onStatusChange: (String) -> Unit
) {
    val statusColor = when (order.status.uppercase()) {
        "PENDING" -> AppColors.warning
        "READY TO PICKUP" -> AppColors.info
        "COMPLETED" -> AppColors.success
        "REFUNDED" -> AppColors.error
        else -> AppColors.textSecondary
    }

    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(order.createdAt.toDate())

    val statusOptions = listOf("PENDING", "READY TO PICKUP", "COMPLETED", "REFUNDED")
    var showStatusMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Order #${order.orderId.takeLast(6)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary,
                        fontSize = 17.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.textSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Status:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.textSecondary,
                            fontSize = 14.sp
                        )

                        Box {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = statusColor.copy(alpha = 0.15f),
                                modifier = Modifier.clickable { showStatusMenu = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = order.status.uppercase(),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = statusColor,
                                        fontSize = 12.sp
                                    )
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Change status",
                                        tint = statusColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showStatusMenu,
                                onDismissRequest = { showStatusMenu = false },
                                modifier = Modifier.background(AppColors.surface)
                            ) {
                                statusOptions.forEach { status ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                status,
                                                color = AppColors.textPrimary,
                                                fontSize = 14.sp
                                            )
                                        },
                                        onClick = {
                                            onStatusChange(status)
                                            showStatusMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "RM ${"%.2f".format(order.totalAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.primary,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${order.items.sumOf { it.quantity }} item(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.textSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onExpandClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = AppColors.primary
                )
            ) {
                Text(
                    if (isExpanded) "Hide Details" else "View Details",
                    fontSize = 13.sp
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = AppColors.divider,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    Text(
                        text = "Order Items",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.textPrimary,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    order.items.forEach { cartItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val bitmap = remember(cartItem.menuItem.imageUrl) {
                                try {
                                    if (cartItem.menuItem.imageUrl.isNotBlank()) {
                                        Base64Utils.base64ToBitmap(cartItem.menuItem.imageUrl)
                                    } else null
                                } catch (e: Exception) {
                                    null
                                }
                            }

                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = cartItem.menuItem.name,
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Image(
                                    painter = painterResource(R.drawable.tomyammaggi),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cartItem.menuItem.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.textPrimary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Qty: ${cartItem.quantity}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.textSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            Text(
                                text = "RM ${"%.2f".format(cartItem.totalPrice)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.textPrimary,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = AppColors.divider
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "User ID:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.textSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = order.userId.takeLast(8),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
