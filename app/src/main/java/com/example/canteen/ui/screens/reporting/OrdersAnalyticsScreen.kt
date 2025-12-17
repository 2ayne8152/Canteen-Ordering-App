package com.example.canteen.ui.screens.reporting

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.canteen.viewmodel.reporting.OrdersAnalyticsViewModel
import com.example.canteen.viewmodel.reporting.UiState
import com.example.canteen.viewmodel.reporting.OrdersAnalyticsData
import com.example.canteen.viewmodel.reporting.MenuItemAnalytics
import com.example.menumanagement.BottomNavigationBar
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersAnalyticsScreen(
    navController: NavController,
    onBack: () -> Unit = { navController.popBackStack() }
) {
    val viewModel: OrdersAnalyticsViewModel = viewModel()
    var selectedPeriod by remember { mutableStateOf("Daily") }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val analyticsData by viewModel.analyticsData.collectAsState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    val periods = listOf("Daily", "Weekly", "Monthly", "Yearly")

    LaunchedEffect(selectedPeriod, selectedDate.timeInMillis) {
        viewModel.loadOrdersAnalytics(selectedPeriod, selectedDate)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Orders Analytics",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            when (val state = analyticsData) {
                is UiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is UiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            viewModel.loadOrdersAnalytics(selectedPeriod, selectedDate)
                        }) {
                            Text("Retry")
                        }
                    }
                }
                is UiState.Success -> {
                    OrdersAnalyticsContent(
                        state = state,
                        selectedPeriod = selectedPeriod,
                        onPeriodChange = { selectedPeriod = it },
                        selectedDate = selectedDate,
                        onDateChange = { selectedDate = it },
                        onShowDatePicker = { showDatePicker = true },
                        currencyFormatter = currencyFormatter
                    )
                }
            }

            // Date Picker Dialog
            if (showDatePicker) {
                val currentDateInUtc = Calendar.getInstance().apply {
                    time = selectedDate.time
                    val year = get(Calendar.YEAR)
                    val month = get(Calendar.MONTH)
                    val day = get(Calendar.DAY_OF_MONTH)
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                    set(year, month, day, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = currentDateInUtc
                )

                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { utcMillis ->
                                    val utcCalendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                                        timeInMillis = utcMillis
                                    }
                                    selectedDate = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
                                        set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
                                        set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                }
                                showDatePicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }
    }
}

@Composable
private fun OrdersAnalyticsContent(
    state: UiState.Success<OrdersAnalyticsData>,
    selectedPeriod: String,
    onPeriodChange: (String) -> Unit,
    selectedDate: Calendar,
    onDateChange: (Calendar) -> Unit,
    onShowDatePicker: () -> Unit,
    currencyFormatter: NumberFormat
) {
    val periods = listOf("Daily", "Weekly", "Monthly", "Yearly")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Period Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            periods.forEach { period ->
                FilterChip(
                    selected = selectedPeriod == period,
                    onClick = { onPeriodChange(period) },
                    label = {
                        Text(
                            period,
                            fontSize = 13.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Date Navigation
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        onDateChange(Calendar.getInstance().apply {
                            time = selectedDate.time
                            when (selectedPeriod) {
                                "Daily" -> add(Calendar.DAY_OF_YEAR, -1)
                                "Weekly" -> add(Calendar.WEEK_OF_YEAR, -1)
                                "Monthly" -> add(Calendar.MONTH, -1)
                                "Yearly" -> add(Calendar.YEAR, -1)
                            }
                        })
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onShowDatePicker) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Select Date",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = when (selectedPeriod) {
                            "Daily" -> SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()).format(selectedDate.time)
                            "Weekly" -> {
                                val weekStart = Calendar.getInstance().apply {
                                    time = selectedDate.time
                                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                                }
                                val weekEnd = Calendar.getInstance().apply {
                                    time = weekStart.time
                                    add(Calendar.DAY_OF_YEAR, 6)
                                }
                                "${SimpleDateFormat("MMM dd", Locale.getDefault()).format(weekStart.time)} - ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(weekEnd.time)}"
                            }
                            "Monthly" -> SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedDate.time)
                            "Yearly" -> SimpleDateFormat("yyyy", Locale.getDefault()).format(selectedDate.time)
                            else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(selectedDate.time)
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = {
                        onDateChange(Calendar.getInstance().apply {
                            time = selectedDate.time
                            when (selectedPeriod) {
                                "Daily" -> add(Calendar.DAY_OF_YEAR, 1)
                                "Weekly" -> add(Calendar.WEEK_OF_YEAR, 1)
                                "Monthly" -> add(Calendar.MONTH, 1)
                                "Yearly" -> add(Calendar.YEAR, 1)
                            }
                        })
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Summary Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Total Orders",
                value = state.data.totalOrders.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Items Sold",
                value = state.data.totalItems.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Revenue",
                value = currencyFormatter.format(state.data.totalRevenue),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Avg Order",
                value = currencyFormatter.format(state.data.averageOrderValue),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Order Trend Chart
        if (state.data.orderTrend.isNotEmpty()) {
            ChartCard(title = "Order Volume") {
                LineChart(
                    dataPoints = state.data.orderTrend,
                    labels = state.data.orderTrendLabels,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Top Selling Items
        if (state.data.topSellingItems.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Top Selling Items",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    state.data.topSellingItems.forEachIndexed { index, item ->
                        if (index > 0) {
                            Divider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${index + 1}. ${item.menuItemName}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${item.totalQuantity} sold • ${item.totalOrders} orders",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = currencyFormatter.format(item.totalRevenue),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${String.format("%.1f", item.percentage)}%",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No orders data available for this period",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}