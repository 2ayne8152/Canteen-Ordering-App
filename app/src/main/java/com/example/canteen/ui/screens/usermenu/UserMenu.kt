package com.example.canteen.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.canteen.R
import com.example.canteen.data.MenuItem

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

    // Tab state
    val categories = listOf("Food", "Beverages", "Dessert")
    var selectedTab by remember { mutableStateOf(0) }

    // Filtered menu items by search and category
    val filteredMenuItems = menuItems.filter {
        it.name.contains(searchQuery, ignoreCase = true) &&
                it.categoryId.equals(categories[selectedTab].lowercase(), ignoreCase = true)
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    placeholder = { Text("Search food...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                TabRow(selectedTabIndex = selectedTab) {
                    categories.forEachIndexed { index, category ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(category) }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "${categories[selectedTab]} Menu",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 30.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            items(filteredMenuItems) { item ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    MenuItemCard(
                        menuItem = item,
                        onClick = {
                            selectedItem = item
                            isSheetOpen = true
                            onItemClick(item)
                        },
                        modifier = Modifier.fillMaxWidth(0.95f)
                    )
                }
            }
        }

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
fun MenuItemCard(
    menuItem: MenuItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (menuItem.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = menuItem.imageUrl,
                    contentDescription = menuItem.name,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.tomyammaggi),
                    contentDescription = menuItem.name,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(menuItem.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(menuItem.description, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text("RM %.2f".format(menuItem.price), style = MaterialTheme.typography.bodyMedium)
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
        shape = MaterialTheme.shapes.large
    ) {
        Text("$numOfItems items • RM ${"%.2f".format(totalPrice)}")
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {

            Text(
                text = item.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = item.description,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "RM %.2f".format(item.price),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFFF2F2F2), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }

                    Text(
                        text = quantity.toString(),
                        fontSize = 18.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    IconButton(
                        onClick = { quantity++ },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Subtotal: RM ${(item.price * quantity).format(2)}",
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(24.dp))

            Column {
                Button(
                    onClick = { onAddToCart(quantity) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Add to Cart • RM ${(item.price * quantity).format(2)}")
                }

                if (cartItemCount > 0) {
                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onViewCart,
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50))
                    ) {
                        Text("View Cart ($cartItemCount)")
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}

// helper
fun Double.format(digits: Int) = "%.${digits}f".format(this)
