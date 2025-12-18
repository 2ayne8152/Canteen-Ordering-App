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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import com.example.canteen.ui.theme.AppColors
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
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Payment History",
                        color = AppColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.surface,
                    titleContentColor = AppColors.textPrimary
                ),
                modifier = Modifier.shadow(4.dp)
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(AppColors.background)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = AppColors.textSecondary
                    )
                },
                placeholder = {
                    Text(
                        "Search by Receipt ID",
                        color = AppColors.textTertiary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.primary,
                    unfocusedBorderColor = AppColors.divider,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    cursorColor = AppColors.primary,
                    focusedContainerColor = AppColors.surface,
                    unfocusedContainerColor = AppColors.surface
                )
            )

            Spacer(Modifier.height(16.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
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

            Spacer(Modifier.height(12.dp))

            // Results count
            Text(
                text = "${filteredList.size} payment(s) found",
                fontSize = 13.sp,
                color = AppColors.textSecondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(12.dp))

            // Payment History List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = AppColors.textTertiary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "No payments found",
                            color = AppColors.textSecondary,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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

                    // Add spacing at the bottom
                    item { Spacer(Modifier.height(8.dp)) }
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
        label = {
            Text(
                label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AppColors.primary,
            selectedLabelColor = AppColors.surface,
            containerColor = AppColors.surface,
            labelColor = AppColors.textSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = AppColors.divider,
            selectedBorderColor = AppColors.primary,
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

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Receipt #${data.first.receiptId.takeLast(6)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = AppColors.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            formatTime(data.first.payment_Date),
                            color = AppColors.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                // Refund Status Badge
                data.second?.status?.let { status ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (status) {
                            "Approved" -> AppColors.success.copy(alpha = 0.2f)
                            "Rejected" -> AppColors.error.copy(alpha = 0.2f)
                            else -> AppColors.warning.copy(alpha = 0.2f)
                        }
                    ) {
                        Text(
                            text = status.uppercase(),
                            color = when (status) {
                                "Approved" -> AppColors.success
                                "Rejected" -> AppColors.error
                                else -> AppColors.warning
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Divider(color = AppColors.divider)

            Spacer(Modifier.height(12.dp))

            // Payment Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Order ID:",
                    color = AppColors.textSecondary,
                    fontSize = 14.sp
                )
                Text(
                    "#${data.first.orderId.takeLast(6)}",
                    color = AppColors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total Payment:",
                    color = AppColors.textSecondary,
                    fontSize = 14.sp
                )
                Text(
                    "RM ${String.format("%.2f", data.first.pay_Amount)}",
                    color = AppColors.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (!expanded) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tap to view details",
                        fontSize = 13.sp,
                        color = AppColors.textTertiary
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = AppColors.textTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Expanded Content
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    Divider(color = AppColors.divider)
                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Order Items:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AppColors.textPrimary
                    )

                    Spacer(Modifier.height(8.dp))

                    order?.items?.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${item.menuItem.name} x${item.quantity}",
                                color = AppColors.textSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "RM ${"%.2f".format(item.totalPrice)}",
                                color = AppColors.textPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Payment Method:",
                            color = AppColors.textSecondary,
                            fontSize = 14.sp
                        )
                        Text(
                            data.first.payment_Method,
                            color = AppColors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Tap to collapse",
                            fontSize = 13.sp,
                            color = AppColors.textTertiary
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = null,
                            tint = AppColors.textTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
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
