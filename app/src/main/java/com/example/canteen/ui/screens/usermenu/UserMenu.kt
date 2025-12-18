package com.example.canteen.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
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
import com.example.canteen.R
import com.example.canteen.data.MenuItem
import com.example.canteen.ui.theme.AppColors
import com.example.canteen.viewmodel.staffMenu.Base64Utils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMenu(
    menuItems: List<MenuItem>,
    totalItemsInCart: Int,
    totalPriceInCart: Double,
    onAddToCart: (MenuItem, Int) -> Unit,
    onViewCart: () -> Unit,
    onItemClick: (MenuItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedItem by remember { mutableStateOf<MenuItem?>(null) }
    var isSheetOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val categories = listOf("Food", "Beverages", "Desserts")
    var selectedTab by remember { mutableStateOf(0) }

    val filteredMenuItems = menuItems.filter {
        it.name.contains(searchQuery, ignoreCase = true) &&
                it.categoryId.equals(categories[selectedTab].lowercase(), ignoreCase = true)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Search food...", color = AppColors.textTertiary)
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = AppColors.textSecondary
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(50.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppColors.surface,
                        unfocusedContainerColor = AppColors.surface,
                        focusedBorderColor = AppColors.divider,
                        unfocusedBorderColor = AppColors.divider,
                        cursorColor = AppColors.primary,
                        focusedTextColor = AppColors.textPrimary,
                        unfocusedTextColor = AppColors.textPrimary
                    )
                )
            }

            // Category Tabs
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEachIndexed { index, category ->
                        val selected = selectedTab == index
                        Surface(
                            color = if (selected) AppColors.primary else AppColors.surface,
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.clickable { selectedTab = index },
                            shadowElevation = if (selected) 0.dp else 2.dp
                        ) {
                            Text(
                                category,
                                color = if (selected) AppColors.surface else AppColors.textPrimary,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                fontSize = 14.sp,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            // Category Title
            item {
                Text(
                    text = "${categories[selectedTab]} Menu",
                    color = AppColors.textPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 28.sp
                )
            }

            // Menu Items
            items(filteredMenuItems) { item ->
                MenuItemCard(
                    menuItem = item,
                    onClick = {
                        if (item.remainQuantity > 0) {
                            selectedItem = item
                            isSheetOpen = true
                            onItemClick(item)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Floating Checkout
        if (totalItemsInCart > 0) {
            FloatingCheckoutBar(
                numOfItems = totalItemsInCart,
                totalPrice = totalPriceInCart,
                onClick = onViewCart,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }

        // Bottom Sheet
        if (isSheetOpen && selectedItem != null) {
            MenuItemCustomization(
                item = selectedItem!!,
                onAddToCart = { qty ->
                    onAddToCart(selectedItem!!, qty)
                    isSheetOpen = false
                },
                onDismiss = { isSheetOpen = false },
                onViewCart = {
                    isSheetOpen = false
                    onViewCart()
                },
                cartItemCount = totalItemsInCart
            )
        }
    }
}

@Composable
fun MenuItemCard(menuItem: MenuItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bitmap: Bitmap? = remember(menuItem.imageUrl) {
        try {
            if (menuItem.imageUrl.isNotBlank()) Base64Utils.base64ToBitmap(menuItem.imageUrl)
            else null
        } catch (e: Exception) { null }
    }

    val imageBitmap = bitmap?.asImageBitmap()
    val isOutOfStock = menuItem.remainQuantity <= 0
    val isLowStock = menuItem.remainQuantity in 1..5

    Card(
        modifier = modifier.clickable(enabled = !isOutOfStock) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOutOfStock) AppColors.surface.copy(alpha = 0.5f) else AppColors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOutOfStock) 0.dp else 2.dp)
    ) {
        Box {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = menuItem.name,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                            alpha = if (isOutOfStock) 0.4f else 1f
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.tomyammaggi),
                            contentDescription = menuItem.name,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                            alpha = if (isOutOfStock) 0.4f else 1f
                        )
                    }

                    if (isOutOfStock) {
                        Surface(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            color = AppColors.background.copy(alpha = 0.7f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "OUT OF\nSTOCK",
                                    color = AppColors.error,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 14.sp,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        menuItem.name,
                        color = if (isOutOfStock) AppColors.disabled else AppColors.textPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        menuItem.description,
                        color = if (isOutOfStock) AppColors.disabled else AppColors.textSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "RM %.2f".format(menuItem.price),
                            color = if (isOutOfStock) AppColors.disabled else AppColors.primary,
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 16.sp
                        )

                        if (isLowStock && !isOutOfStock) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AppColors.warning.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "Only ${menuItem.remainQuantity} left",
                                    color = AppColors.warning,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingCheckoutBar(
    numOfItems: Int,
    totalPrice: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.primary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text(
            "$numOfItems items • RM ${"%.2f".format(totalPrice)}",
            color = AppColors.surface,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 16.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemCustomization(
    item: MenuItem,
    onAddToCart: (Int) -> Unit,
    onDismiss: () -> Unit,
    onViewCart: () -> Unit,
    cartItemCount: Int
) {
    var quantity by remember { mutableStateOf(1) }
    val maxQuantity = item.remainQuantity.coerceAtLeast(1) // Available stock

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppColors.sheet,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Text(
                item.name,
                color = AppColors.textPrimary,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(6.dp))
            Text(
                item.description,
                color = AppColors.textSecondary,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(12.dp))

            // Stock indicator
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (maxQuantity < 5) AppColors.warning.copy(alpha = 0.15f)
                else AppColors.success.copy(alpha = 0.15f)
            ) {
                Text(
                    "Available: $maxQuantity",
                    color = if (maxQuantity < 5) AppColors.warning else AppColors.success,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "RM %.2f".format(item.price),
                    color = AppColors.primary,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 24.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(AppColors.surface, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        enabled = quantity > 1
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Decrease quantity",
                            tint = if (quantity > 1) AppColors.textPrimary else AppColors.disabled
                        )
                    }
                    Text(
                        quantity.toString(),
                        color = AppColors.textPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    IconButton(
                        onClick = { if (quantity < maxQuantity) quantity++ },
                        enabled = quantity < maxQuantity
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Increase quantity",
                            tint = if (quantity < maxQuantity) AppColors.textPrimary else AppColors.disabled
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { onAddToCart(quantity) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.primary
                )
            ) {
                Text(
                    "Add to Cart • RM ${(item.price * quantity).format(2)}",
                    color = AppColors.surface,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp
                )
            }

            if (cartItemCount > 0) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onViewCart,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(50.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.5.dp,
                        brush = androidx.compose.ui.graphics.SolidColor(AppColors.primary)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppColors.primary
                    )
                ) {
                    Text(
                        "View Cart ($cartItemCount)",
                        color = AppColors.primary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

fun Double.format(digits: Int) = "%.${digits}f".format(this)