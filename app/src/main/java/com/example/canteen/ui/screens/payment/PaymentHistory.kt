package com.example.canteen.ui.screens.payment

import android.os.Build
import android.text.format.DateUtils.isToday
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.canteen.ui.theme.CanteenTheme
import com.example.canteen.ui.theme.lightBlue
import com.example.canteen.ui.theme.lightViolet
import com.example.canteen.data.Receipt
import com.example.canteen.data.RefundRequest
import com.example.canteen.viewmodel.payment.ReceiptViewModel
import com.example.canteen.viewmodel.usermenu.OrderViewModel
import com.example.menumanagement.BottomNavigationBar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

enum class TimePeriod {
    ALL, TODAY, WEEK, MONTH
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistory(
    navController: NavController,
    receiptViewModel: ReceiptViewModel,
    orderViewModel: OrderViewModel
) {
    val allReceipt by receiptViewModel.receiptList.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf(TimePeriod.ALL) }

    // Store which item is expanded
    val expandedMap = remember { mutableStateMapOf<String, Boolean>() }

    // Filter by search query first
    val searchFiltered = allReceipt.filter { pair ->
        val receipt = pair.first
        receipt.receiptId.contains(searchQuery.trim(), ignoreCase = true)
    }

    // Then filter by time period
    val filteredList = when (selectedPeriod) {
        TimePeriod.ALL -> searchFiltered
        TimePeriod.TODAY -> searchFiltered.filter { isToday(it.first.payment_Date) }
        TimePeriod.WEEK -> searchFiltered.filter { isThisWeek(it.first.payment_Date) }
        TimePeriod.MONTH -> searchFiltered.filter { isThisMonth(it.first.payment_Date) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment History") },
                modifier = Modifier.shadow(6.dp)
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(8.dp)
        ) {
            Spacer(Modifier.height(6.dp))

            // -----------------------------------------------------
            // Search Bar
            // -----------------------------------------------------
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black)
                },
                placeholder = { Text("Search by Receipt ID", color = Color.Black) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(lightViolet, shape = RoundedCornerShape(16))
            )

            Spacer(Modifier.height(12.dp))

            // -----------------------------------------------------
            // Filter Chips
            // -----------------------------------------------------
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp)
            ) {
                item {
                    FilterChipItem(
                        label = "All",
                        selected = selectedPeriod == TimePeriod.ALL,
                        onClick = { selectedPeriod = TimePeriod.ALL }
                    )
                }
                item {
                    FilterChipItem(
                        label = "Today",
                        selected = selectedPeriod == TimePeriod.TODAY,
                        onClick = { selectedPeriod = TimePeriod.TODAY }
                    )
                }
                item {
                    FilterChipItem(
                        label = "This Week",
                        selected = selectedPeriod == TimePeriod.WEEK,
                        onClick = { selectedPeriod = TimePeriod.WEEK }
                    )
                }
                item {
                    FilterChipItem(
                        label = "This Month",
                        selected = selectedPeriod == TimePeriod.MONTH,
                        onClick = { selectedPeriod = TimePeriod.MONTH }
                    )
                }
            }

            Spacer(Modifier.height(2.dp))

            // Results count
            Text(
                text = "${filteredList.size} payment(s) found",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 12.dp)
            )

            Spacer(Modifier.height(4.dp))

            // -----------------------------------------------------
            // Payment History List
            // -----------------------------------------------------
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, top = 2.dp, end = 12.dp)
            ) {
                items(filteredList) { receipt ->
                    val expanded = expandedMap[receipt.first.receiptId] ?: false

                    PaymentHistoryCard(
                        orderViewModel = orderViewModel,
                        data = receipt,
                        expanded = expanded,
                        onClick = {
                            expandedMap[receipt.first.receiptId] = !expanded
                        }
                    )
                }

                // Empty state
                if (filteredList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Text(
                                text = "No payments found for this period",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = Color.White,
            containerColor = Color.LightGray.copy(alpha = 0.3f),
            labelColor = Color.Black
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
            selectedBorderColor = MaterialTheme.colorScheme.primary,
            borderWidth = 1.dp,
            selectedBorderWidth = 2.dp
        )
    )
}

// Helper functions to check date periods
@RequiresApi(Build.VERSION_CODES.O)
fun isToday(timestampMillis: Long): Boolean {
    val paymentDate = Date(timestampMillis).toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val today = LocalDate.now()
    return paymentDate.isEqual(today)
}

@RequiresApi(Build.VERSION_CODES.O)
fun isThisWeek(timestampMillis: Long): Boolean {
    val paymentDate = Date(timestampMillis).toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val today = LocalDate.now()
    val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
    val endOfWeek = startOfWeek.plusDays(6)
    return !paymentDate.isBefore(startOfWeek) && !paymentDate.isAfter(endOfWeek)
}

@RequiresApi(Build.VERSION_CODES.O)
fun isThisMonth(timestampMillis: Long): Boolean {
    val paymentDate = Date(timestampMillis).toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val today = LocalDate.now()
    return paymentDate.year == today.year && paymentDate.month == today.month
}

@Composable
fun PaymentHistoryCard(
    orderViewModel: OrderViewModel,
    data: Pair<Receipt, RefundRequest?>,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val orders by orderViewModel.orders.collectAsState()
    val order = orders[data.first.orderId]

    LaunchedEffect(expanded) {
        if (expanded) {
            orderViewModel.getOrderForHistory(data.first.orderId)
        }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = lightBlue,
        shadowElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
    ) {
        val formatted = formatTime(data.first.payment_Date)
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "ReceiptID: ${data.first.receiptId.takeLast(6)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text("${formatted}", color = Color.Black)
            }

            Spacer(Modifier.height(4.dp))

            Text("Order ID :  ${data.first.orderId.takeLast(n=6)}", color = Color.Black)
            Text(
                "Total Payment : RM${String.format("%.2f", data.first.pay_Amount)}",
                color = Color.Black
            )
            Text("Refund : ${data.second?.status ?: "None"}", color = Color.Black)
            if (!expanded) {
                Text(
                    text = "Tap to view more",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            // ▼▼▼ ONLY SHOW WHEN EXPANDED ▼▼▼
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Divider()

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Order Items :",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )

                    order?.items?.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.menuItem.name} x${item.quantity}")
                            Text("RM ${"%.2f".format(item.totalPrice)}")
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text("Payment Method : ${data.first.payment_Method}", color = Color.Black)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentHistoryPreview() {
    CanteenTheme {
        //PaymentHistory(viewModel ())
    }
}
