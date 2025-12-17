package com.example.canteen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.canteen.data.CartItem
import com.example.canteen.viewmodel.usermenu.CartViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import com.example.canteen.R

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

    Scaffold()
    { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .padding(16.dp)) {

            if (cart.value.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Your cart is empty.", fontSize = 18.sp)
                }
                return@Column
            }

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
            }

            Divider()

            // Totals & Checkout
            Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Items", fontWeight = FontWeight.Medium)
                    Text("$totalItems")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", fontWeight = FontWeight.Bold)
                    Text("RM ${"%.2f".format(totalPrice)}", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onCheckout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Checkout • RM ${"%.2f".format(totalPrice)}")
                }

                OutlinedButton(
                    onClick = { cartViewModel.clearCart() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear cart")
                }
            }
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
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (cartItem.menuItem.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = cartItem.menuItem.imageUrl,
                    contentDescription = cartItem.menuItem.name,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))
                )
            } else {
                // fallback placeholder from drawable
                Icon(painter = painterResource(id = R.drawable.tomyammaggi), contentDescription = null, modifier = Modifier.size(80.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(cartItem.menuItem.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("RM ${"%.2f".format(cartItem.menuItem.price)}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Subtotal: RM ${"%.2f".format(cartItem.totalPrice)}", fontWeight = FontWeight.SemiBold)
            }

            // Quantity controls (compact)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onIncrease) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
                Text("${cartItem.quantity}", fontWeight = FontWeight.Medium)
                IconButton(onClick = onDecrease) {
                    Icon(Icons.Default.Remove, contentDescription = "Remove")
                }
                TextButton(onClick = onRemove) {
                    Text("Remove")
                }
            }
        }
    }
}
