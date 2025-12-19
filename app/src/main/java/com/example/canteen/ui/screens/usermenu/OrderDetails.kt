package com.example.canteen.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.canteen.R
import com.example.canteen.data.CartItem
import com.example.canteen.ui.screens.payment.RefundDetailScreen
import com.example.canteen.ui.theme.AppColors
import com.example.canteen.viewmodel.payment.ReceiptViewModel
import com.example.canteen.viewmodel.staffMenu.Base64Utils
import com.example.canteen.viewmodel.usermenu.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onBack: () -> Unit,
    onClick: () -> Unit,
    orderViewModel: OrderViewModel,
    receiptViewModel: ReceiptViewModel
) {
    // Listen to order in real-time
    val order by orderViewModel.currentOrder.collectAsState()

    LaunchedEffect(orderId) {
        Log.d("OrderListener", "Start listening orderId=$orderId")
        orderViewModel.startListeningOrder(orderId)
        receiptViewModel.loadReceiptByOrderId(orderId)
    }

    DisposableEffect(Unit) {
        onDispose {
            orderViewModel.stopListeningOrder()
        }
    }

    // Loading state
    if (order == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val currentOrder = order!!
    val totalItems = currentOrder.items.sumOf { it.quantity }
    val totalPrice = currentOrder.totalAmount
    val status = currentOrder.status == "PENDING" ||
            currentOrder.status == "PREPARING" ||
            currentOrder.status == "Pending"

    val receiptPair by receiptViewModel.receiptLoadByOrderId.collectAsState()

    val statusColor = when (currentOrder.status.uppercase()) {
        "PENDING" -> AppColors.warning
        "READY TO PICKUP" -> AppColors.info
        "COMPLETED" -> AppColors.success
        "REFUNDED" -> AppColors.error
        else -> AppColors.textSecondary
    }

    LaunchedEffect(receiptPair) {
        receiptPair?.let {
            Log.d("OrderListener", "Receipt loaded for orderId=${it.first.orderId}")
        }
    }

    Scaffold(
        containerColor = AppColors.background
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(AppColors.background)
                .padding(16.dp)
        ) {

            // ===== Order Info Card =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Order #${currentOrder.orderId.takeLast(6)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = AppColors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.textSecondary
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = statusColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = currentOrder.status.uppercase(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (currentOrder.status == "REFUNDED") {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = AppColors.divider
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        RefundDetailScreen(
                            orderId = currentOrder.orderId,
                            receiptViewModel = receiptViewModel
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== Section Title =====
            Text(
                text = "Order Items",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ===== Order Items =====
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(currentOrder.items) { cartItem ->
                    OrderItemRow(cartItem = cartItem)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                thickness = 1.dp,
                color = AppColors.divider
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ===== Totals =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Total Items",
                            color = AppColors.textSecondary
                        )
                        Text(
                            "$totalItems",
                            color = AppColors.textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = AppColors.divider
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Total Amount",
                            color = AppColors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "RM ${"%.2f".format(totalPrice)}",
                            color = AppColors.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
            }

            if (status) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.error
                    )
                ) {
                    Text(
                        text = "Request Refund",
                        color = AppColors.surface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OrderItemRow(cartItem: CartItem) {
    val bitmap = remember(cartItem.menuItem.imageUrl) {
        try {
            if (cartItem.menuItem.imageUrl.isNotBlank()) {
                Base64Utils.base64ToBitmap(cartItem.menuItem.imageUrl)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = cartItem.menuItem.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.tomyammaggi),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    cartItem.menuItem.name,
                    color = AppColors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "RM ${"%.2f".format(cartItem.menuItem.price)}",
                    color = AppColors.textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Qty: ${cartItem.quantity}",
                        color = AppColors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "RM ${"%.2f".format(cartItem.totalPrice)}",
                        color = AppColors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
