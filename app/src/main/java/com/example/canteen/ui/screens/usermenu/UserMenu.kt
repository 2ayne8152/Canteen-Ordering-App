package com.example.canteen.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.canteen.R
import com.example.canteen.data.MenuItem

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
    var quantity by remember { mutableStateOf(1) }

    Box(modifier = modifier.fillMaxSize()) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Main Menu",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 30.sp,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(menuItems) { item ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    MenuItemCard(
                        menuItem = item,
                        onClick = {
                            selectedItem = item
                            quantity = 1
                            onItemClick(item)
                        },
                        modifier = Modifier.fillMaxWidth(0.95f) // card will take 95% of width
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

        selectedItem?.let { item ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                MenuItemCustomization(
                    itemName = item.name,
                    itemImageUrl = item.imageUrl,
                    placeholderRes = R.drawable.tomyammaggi,
                    quantity = quantity,
                    onIncrease = { if (quantity < item.remainQuantity) quantity++ },
                    onDecrease = { if (quantity > 1) quantity-- },
                    onAddToCart = {
                        onAddToCart(item, quantity)
                        selectedItem = null
                    },
                    onCancel = { selectedItem = null }
                )
            }
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {

            if (menuItem.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = menuItem.imageUrl,
                    contentDescription = menuItem.name,
                    modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.tomyammaggi),
                    contentDescription = menuItem.name,
                    modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)),
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
        modifier = modifier.fillMaxWidth().height(60.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Text("$numOfItems items • RM ${"%.2f".format(totalPrice)}")
    }
}

@Composable
fun MenuItemCustomization(
    itemName: String,
    itemImageUrl: String,
    @DrawableRes placeholderRes: Int = R.drawable.tomyammaggi,
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onAddToCart: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {

            if (itemImageUrl.isNotBlank()) {
                AsyncImage(
                    model = itemImageUrl,
                    contentDescription = itemName,
                    modifier = Modifier
                        .height(160.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(placeholderRes),
                    contentDescription = itemName,
                    modifier = Modifier
                        .height(160.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(itemName, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = onDecrease) { Text("-") }
                Spacer(Modifier.width(16.dp))
                Text(quantity.toString(), style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.width(16.dp))
                OutlinedButton(onClick = onIncrease) { Text("+") }
            }

            Spacer(Modifier.height(12.dp))
            Button(onClick = onAddToCart, modifier = Modifier.fillMaxWidth()) { Text("Add to Cart") }
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}
