package com.example.canteen.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import coil.compose.AsyncImage
import com.example.canteen.R
import com.example.canteen.data.CartItem
import com.example.canteen.data.Order
import com.example.canteen.ui.screens.payment.RefundDetailScreen
import com.example.canteen.ui.theme.Green
import com.example.canteen.ui.theme.darkGreen
import com.example.canteen.viewmodel.payment.ReceiptViewModel
import com.example.canteen.viewmodel.staffMenu.Base64Utils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    order: Order,
    onBack: () -> Unit,
    onClick: () -> Unit,
    receiptViewModel: ReceiptViewModel
) {
    val totalItems = order.items.sumOf { it.quantity }
    val totalPrice = order.totalAmount
    val status = order.status == "PENDING" || order.status == "PREPARING" || order.status == "Pending"
    val receiptPair by receiptViewModel.receiptLoadByOrderId.collectAsState()

    LaunchedEffect(order.orderId) {
        Log.d("OrderListener", order.orderId)
        receiptViewModel.loadReceiptByOrderId(order.orderId)
    }

    LaunchedEffect(receiptPair) {
        receiptPair?.let {
            Log.d("OrderListener", "Receipt loaded for orderId=${it.first.orderId}")
        }
    }


    Scaffold()
    { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // ===== Order Info =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Order ID: ${order.orderId}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Status: ${order.status}",
                        fontWeight = FontWeight.Medium
                    )

                    if (receiptPair?.first?.refundId != null){
                        Spacer(modifier = Modifier.height(10.dp))

                        Divider(modifier = Modifier.padding(bottom = 8.dp))
                        RefundDetailScreen(orderId = order.orderId, receiptViewModel = receiptViewModel)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== Order Items =====
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(order.items) { cartItem ->
                    OrderItemRow(cartItem = cartItem)
                }
            }

            Divider()

            // ===== Totals =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Items", fontWeight = FontWeight.Medium)
                    Text("$totalItems")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total", fontWeight = FontWeight.Bold)
                    Text(
                        "RM ${"%.2f".format(totalPrice)}",
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (status){
                    Button(
                        onClick = onClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(darkGreen))
                    {
                        Text(text = "Refund Request")
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
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
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
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.tomyammaggi),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(cartItem.menuItem.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("RM ${"%.2f".format(cartItem.menuItem.price)}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Qty: ${cartItem.quantity}", fontWeight = FontWeight.Medium)
                Text(
                    "Subtotal: RM ${"%.2f".format(cartItem.totalPrice)}",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
