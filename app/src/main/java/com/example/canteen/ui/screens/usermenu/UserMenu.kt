package com.example.canteen.ui.screens.usermenu

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.canteen.R
import com.example.canteen.data.MenuItem
import com.example.canteen.data.menuItems
import com.example.canteen.ui.theme.CanteenTheme

@Composable
fun UserMenu(
    menuItems: List<MenuItem>,
    numOfItem: Int,
    totalPrice: Double,
    onItemClick: (MenuItem) -> Unit,
    onDetailClick: () -> Unit
) {
    // Track the selected item for customization
    var selectedItem by remember { mutableStateOf<MenuItem?>(null) }
    var quantity by remember { mutableStateOf(1) }

    Scaffold(
        topBar = {},
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 110.dp)
            ) {

                item {
                    Text(
                        text = "Main Menu",
                        style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                        fontSize = 30.sp,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                items(menuItems) { item ->
                    MenuItemCard(
                        imageRes = item.imageRes,
                        itemName = item.itemName,
                        itemDesc = item.itemDesc,
                        itemPrice = item.itemPrice,
                        modifier = Modifier.fillMaxWidth(),
                        onItemClick = {
                            selectedItem = item   // <-- open customization
                            quantity = 1          // reset
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

            // Show customization overlay when an item is selected
            selectedItem?.let { sel ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    MenuItemCustomization(
                        // convert @StringRes -> String for display
                        itemName = stringResource(sel.itemName),
                        itemImage = sel.imageRes,
                        quantity = quantity,
                        onIncrease = { quantity++ },
                        onDecrease = { if (quantity > 1) quantity-- },
                        onAddToCart = {
                            // TODO: Add to cart logic (use sel & quantity)
                            selectedItem = null // close popup
                        },
                        onCancel = {
                            selectedItem = null
                        },
                        modifier = Modifier
                    )
                }
            }
        }
    }
}

@Composable
fun MenuItemCard(
    @DrawableRes imageRes: Int,
    @StringRes itemName: Int,
    @StringRes itemDesc: Int,
    itemPrice: Double,
    modifier: Modifier = Modifier,
    onItemClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onItemClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // IMAGE (64dp)
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // COLUMN made same width as image (64dp)
            Column(
                modifier = Modifier.height(64.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = stringResource(itemName),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = stringResource(itemDesc),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = String.format("RM %.2f", itemPrice),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Bottom)
            )
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "$numOfItem items ordered",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Tap to view details",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            Text(
                text = String.format("RM %.2f", totalPrice),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun MenuItemCustomization(
    itemName: String,
    itemImage: Int,
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onAddToCart: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Top Row: Title + Cancel Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Customize Order",
                    style = MaterialTheme.typography.titleLarge
                )

                // Cancel Button (X)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onCancel() }
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✕",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Item Image
            Image(
                painter = painterResource(id = itemImage),
                contentDescription = itemName,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            // Item Name
            Text(
                text = itemName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            // Quantity Label
            Text(
                text = "Select Quantity",
                style = MaterialTheme.typography.titleMedium
            )

            // Quantity Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                // Minus Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onDecrease() }
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "–",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))

                Text(
                    text = quantity.toString(),
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.width(32.dp))

                // Plus Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onIncrease() }
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            // Add to Cart Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clickable { onAddToCart() },
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add to Cart",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun UserMenuPreview(){
    CanteenTheme {

    }
}