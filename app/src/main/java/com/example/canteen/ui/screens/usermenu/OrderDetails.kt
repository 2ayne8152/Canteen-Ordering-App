package com.example.canteen.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import coil.compose.AsyncImage
import com.example.canteen.R
import com.example.canteen.data.CartItem
import com.example.canteen.data.Order
import com.example.canteen.ui.theme.AppColors
import com.example.canteen.viewmodel.staffMenu.Base64Utils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    order: Order,
    onBack: () -> Unit
) {
    val totalItems = order.items.sumOf { it.quantity }
    val totalPrice = order.totalAmount

    val statusColor = when (order.status.uppercase()) {
        "PENDING" -> AppColors.warning
        "READY TO PICKUP" -> AppColors.info
        "COMPLETED" -> AppColors.success
        "REFUNDED" -> AppColors.error
        else -> AppColors.textSecondary
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
                        text = "Order #${order.orderId.takeLast(6)}",
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
                                text = order.status.uppercase(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp
                            )
                        }
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
                items(order.items) { cartItem ->
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
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppColors.textSecondary
                        )
                        Text(
                            "$totalItems",
                            style = MaterialTheme.typography.bodyLarge,
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
                            style = MaterialTheme.typography.titleMedium,
                            color = AppColors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "RM ${"%.2f".format(totalPrice)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = AppColors.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItemRow(
    cartItem: CartItem
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
                    style = MaterialTheme.typography.titleMedium,
                    color = AppColors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "RM ${"%.2f".format(cartItem.menuItem.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Qty: ${cartItem.quantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "RM ${"%.2f".format(cartItem.totalPrice)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppColors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}