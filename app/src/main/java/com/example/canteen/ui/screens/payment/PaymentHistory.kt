package com.example.canteen.ui.screens.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.canteen.ui.theme.CanteenTheme
import com.example.canteen.ui.theme.gray
import com.example.canteen.ui.theme.lightBlue
import com.example.canteen.ui.theme.lightViolet
import com.example.canteen.ui.theme.middleGray
import com.example.canteen.ui.theme.veryLightBlue
import com.example.canteen.ui.theme.veryLightViolet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistory(
    onBack: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    // Sample data
    val payments = listOf(
        PaymentRecord("R0001", "14:00\n12/6/2025", 15.00, "Credit Card", "-"),
        PaymentRecord("R0002", "13:15\n12/6/2025", 10.20, "TNG", "Pending")
    )

    val filtered = payments.filter { it.id.contains(searchQuery, ignoreCase = true) }

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

            Spacer(Modifier.height(16.dp))

            // -----------------------------------------------------
            // Table Container
            // -----------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(lightBlue, RoundedCornerShape(12.dp))
            ) {

                Column(modifier = Modifier.padding(4.dp)) {

                    // ---------------- HEADER ROW ----------------
                    Card(colors = CardDefaults.cardColors(
                        containerColor = veryLightBlue)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TableHeader("ReceiptID", 0.18f)
                            TableHeader(" Date", 0.22f)
                            TableHeader("Amount", 0.18f)
                            TableHeader("Method", 0.22f)
                            TableHeader("Refund", 0.20f)
                        }
                    }
                    Divider(color = middleGray)

                    Spacer(Modifier.height(8.dp))

                    // ---------------- PAYMENT ROWS ----------------
                    filtered.forEach {
                        PaymentRow(record = it)
                        Divider(color = gray.copy(alpha = 0.4f))
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.TableHeader(title: String, weight: Float) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .weight(weight)
            .padding(4.dp) // optional
    )
}


@Composable
fun PaymentRow(record: PaymentRecord) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Text(record.id, modifier = Modifier.weight(0.18f))
        Text(record.date, modifier = Modifier.weight(0.22f))
        Text(String.format("%.2f", record.amount), modifier = Modifier.weight(0.18f))
        Text(record.method, modifier = Modifier.weight(0.22f))
        Text(record.refundStatus, modifier = Modifier.weight(0.20f))
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentHistoryPreview() {
    CanteenTheme {
        PaymentHistory()
    }
}
