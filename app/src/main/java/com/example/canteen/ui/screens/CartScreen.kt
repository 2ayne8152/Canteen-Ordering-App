package com.example.canteen.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.canteen.data.CartItem
import com.example.canteen.ui.theme.AppColors
import com.example.canteen.viewmodel.usermenu.CartViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import com.example.canteen.R
import com.example.canteen.viewmodel.staffMenu.Base64Utils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    onBack: () -> Unit,
    onCheckout: () -> Unit
) {
    val cart = cartViewModel.cart.collectAsState()
    val totalItems = cart.value.sumOf { it.quantity }
    val totalPrice = cart.value.sumOf { it.totalPrice }

    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = AppColors.background
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(AppColors.background)
        ) {
            if (cart.value.isEmpty()) {
                // Empty State
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AppColors.surface,
                            modifier = Modifier.size(120.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp),
                                    tint = AppColors.textSecondary.copy(alpha = 0.4f)
                                )
                            }
                        }
                        Text(
                            "Your cart is empty",
                            fontSize = 20.sp,
                            color = AppColors.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Add delicious items to get started",
                            fontSize = 14.sp,
                            color = AppColors.textSecondary
                        )
                    }
                }
            } else {
                // Cart with items
                Column(modifier = Modifier.fillMaxSize()) {
                    // Items List
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(cart.value) { cartItem ->
                            CartListItem(
                                cartItem = cartItem,
                                onIncrease = {
                                    cartViewModel.updateQuantity(cartItem.menuItem.id, cartItem.quantity + 1)
                                },
                                onDecrease = {
                                    val newQ = cartItem.quantity - 1
                                    cartViewModel.updateQuantity(cartItem.menuItem.id, newQ)
                                },
                                onRemove = {
                                    cartViewModel.removeItem(cartItem.menuItem.id)
                                }
                            )
                        }

                        // Add spacing at bottom for checkout section
                        item {
                            Spacer(modifier = Modifier.height(180.dp))
                        }
                    }

                    // Floating Checkout Section
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = AppColors.surface,
                        shadowElevation = 12.dp,
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Order Summary
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Total Items",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppColors.textSecondary,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        "$totalItems items",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = AppColors.textPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "Total Amount",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppColors.textSecondary,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        "RM ${"%.2f".format(totalPrice)}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = AppColors.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp
                                    )
                                }
                            }

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Clear Cart Button
                                OutlinedButton(
                                    onClick = { showClearDialog = true },
                                    modifier = Modifier
                                        .weight(0.35f)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        width = 1.5.dp,
                                        brush = androidx.compose.ui.graphics.SolidColor(AppColors.error)
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = AppColors.error
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Clear",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Checkout Button
                                Button(
                                    onClick = onCheckout,
                                    modifier = Modifier
                                        .weight(0.65f)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppColors.primary
                                    )
                                ) {
                                    Text(
                                        "Checkout",
                                        color = AppColors.surface,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Clear Cart Confirmation Dialog
        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                icon = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = AppColors.error,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        "Clear Cart?",
                        color = AppColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        "Are you sure you want to remove all items from your cart? This action cannot be undone.",
                        color = AppColors.textSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            cartViewModel.clearCart()
                            showClearDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.error
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Clear Cart", color = AppColors.surface)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showClearDialog = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = AppColors.textPrimary
                        )
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = AppColors.surface,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun CartListItem(
    cartItem: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
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
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = cartItem.menuItem.name,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.tomyammaggi),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                // Item Details
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        cartItem.menuItem.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )

                    Text(
                        "RM ${"%.2f".format(cartItem.menuItem.price)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.textSecondary,
                        fontSize = 14.sp
                    )

                    Text(
                        "Subtotal: RM ${"%.2f".format(cartItem.totalPrice)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppColors.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                thickness = 1.dp,
                color = AppColors.divider
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quantity Controls and Remove Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity Controls
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AppColors.background,
                    modifier = Modifier.height(44.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        IconButton(
                            onClick = onDecrease,
                            enabled = cartItem.quantity > 1,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Decrease",
                                tint = if (cartItem.quantity > 1) AppColors.primary else AppColors.disabled,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            "${cartItem.quantity}",
                            fontWeight = FontWeight.Bold,
                            color = AppColors.textPrimary,
                            fontSize = 18.sp,
                            modifier = Modifier.widthIn(min = 30.dp),
                            style = MaterialTheme.typography.titleMedium
                        )

                        IconButton(
                            onClick = onIncrease,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Increase",
                                tint = AppColors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Remove Button
                TextButton(
                    onClick = onRemove,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = AppColors.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Remove",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}