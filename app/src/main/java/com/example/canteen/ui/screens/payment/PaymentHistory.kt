package com.example.canteen.ui.screens.payment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.canteen.data.PaymentRecord
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.canteen.ui.theme.CanteenTheme
import com.example.canteen.ui.theme.gray
import com.example.canteen.ui.theme.lightBlue
import com.example.canteen.ui.theme.lightViolet
import com.example.canteen.ui.theme.middleGray
import com.example.canteen.ui.theme.veryLightBlue
import com.example.canteen.ui.theme.veryLightViolet
import com.example.canteen.data.Receipt
import com.example.canteen.data.RefundRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistory(
    onBack: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    // Sample data
    val payments = listOf(
        Receipt(receiptId = "R0001", orderId = "O0001", payment_Date = 1733985600L, pay_Amount = 15.00, payment_Method = "Credit Card", refund = null),
        Receipt(receiptId = "R0002", orderId = "O0002", payment_Date = 1733900000L, pay_Amount = 10.20, payment_Method = "TNG", refund = RefundRequest(status = "pending"))
    )

    val filtered = payments.filter {
        it.receiptId.contains(searchQuery, ignoreCase = true)
    }

    // Store which item is expanded
    val expandedMap = remember { mutableStateMapOf<String, Boolean>() }

    //val filtered = payments.filter { it.id.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(8.dp)
        ) {

            // -----------------------------------------------------
            // Search Bar
            // -----------------------------------------------------
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                placeholder = { Text("Search by Receipt ID") },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(lightViolet, shape = RoundedCornerShape(16))
            )

            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                items(filtered) { receipt ->

                    val expanded = expandedMap[receipt.receiptId] ?: false

                    PaymentHistoryCard(
                        data = receipt,
                        expanded = expanded,
                        onClick = {
                            expandedMap[receipt.receiptId] = !expanded
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentHistoryCard(
    data: Receipt,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = lightBlue,
        shadowElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
    ) {
        val formatted = formatTime(data.payment_Date)
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Receipt ${data.receiptId}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text("${formatted}")
            }

            Spacer(Modifier.height(4.dp))

            Text("Order ID :  ${data.orderId}")
            Text("Total Payment : RM${String.format("%.2f", data.pay_Amount)}")
            Text("Refund : ${data.refund?.status}")

            // ▼▼▼ ONLY SHOW WHEN EXPANDED ▼▼▼
            AnimatedVisibility(visible = expanded) {

                Column {
                    Spacer(Modifier.height(8.dp))
                    Divider()

                    Spacer(Modifier.height(8.dp))

                    Text("Method : ${data.payment_Method}")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentHistoryPreview() {
    CanteenTheme {
        PaymentHistory()
    }
}
