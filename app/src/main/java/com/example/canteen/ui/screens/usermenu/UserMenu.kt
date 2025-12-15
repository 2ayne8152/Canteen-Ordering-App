package com.example.canteen.ui.screens.usermenu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.canteen.viewmodel.login.FirestoreMenuItem

@Composable
fun UserMenu(
    menuItems: List<FirestoreMenuItem>,
    numOfItem: Int,
    totalPrice: Double,
    onItemClick: (FirestoreMenuItem) -> Unit,
    onDetailClick: () -> Unit
) {
    var selectedItem by remember { mutableStateOf<FirestoreMenuItem?>(null) }
    var quantity by remember { mutableStateOf(1) }

    Scaffold(topBar = {}) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 110.dp)
            ) {
                item {
                    Text(
                        text = "Main Menu",
                        fontSize = 30.sp,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                items(menuItems) { item ->
                    MenuItemCard(
                        item = item,
                        modifier = Modifier.fillMaxWidth(),
                        onItemClick = {
                            selectedItem = item
                            quantity = 1
                            onItemClick(item)
                        }
                    )
                }
            }

            if (numOfItem > 0) {
                ViewDetailButton(
                    numOfItem = numOfItem,
                    totalPrice = totalPrice,
                    onDetailClick = onDetailClick,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }

            // Customization overlay
            selectedItem?.let { sel ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    MenuItemCustomization(
                        itemName = sel.name,
                        itemImageUrl = sel.imageUrl,
                        quantity = quantity,
                        onIncrease = { quantity++ },
                        onDecrease = { if (quantity > 1) quantity-- },
                        onAddToCart = {
                            // TODO: Add to cart logic
                            selectedItem = null
                        },
                        onCancel = { selectedItem = null }
                    )
                }
            }
        }
    }
}

@Composable
fun MenuItemCard(
    item: FirestoreMenuItem,
    modifier: Modifier = Modifier,
    onItemClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
            .clickable { onItemClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(item.imageUrl),
                contentDescription = item.name,
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(item.name, fontSize = 18.sp)
                Text(item.description, fontSize = 14.sp, color = Color.Gray)
            }

            Text(
                text = String.format("RM %.2f", item.price),
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun MenuItemCustomization(
    itemName: String,
    itemImageUrl: String,
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onAddToCart: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(itemName, fontSize = 20.sp)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onCancel() }
                        .border(1.dp, Color.Gray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕")
                }
            }

            Image(
                painter = rememberAsyncImagePainter(itemImageUrl),
                contentDescription = itemName,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onDecrease() }
                        .border(1.dp, Color.Gray, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("–") }

                Spacer(modifier = Modifier.width(32.dp))
                Text(quantity.toString(), fontSize = 20.sp)
                Spacer(modifier = Modifier.width(32.dp))

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onIncrease() }
                        .border(1.dp, Color.Gray, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("+") }
            }

            Button(
                onClick = onAddToCart,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Add to Cart") }
        }
    }
}

@Composable
fun ViewDetailButton(
    numOfItem: Int,
    totalPrice: Double,
    onDetailClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onDetailClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("$numOfItem items ordered")
                Text(String.format("Total: RM %.2f", totalPrice), color = Color.Gray)
            }
            Text(String.format("RM %.2f", totalPrice))
        }
    }
}
