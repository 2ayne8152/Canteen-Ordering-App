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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import com.example.canteen.viewmodel.reporting.UiState
import com.example.canteen.viewmodel.reporting.ReportViewModel
import com.example.canteen.ui.screens.CanteenScreen
import com.example.menumanagement.BottomNavigationBar
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.cos
import kotlin.math.sin

// Custom dark theme colors to match screenshot
private val DarkBackground = Color(0xFF1A1A1A)
private val CardBackground = Color(0xFF2A2A2A)
private val AccentOrange = Color(0xFFFF6B35)
private val AccentYellow = Color(0xFFFFC107)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFB0B0B0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    navController: NavController,
    onBack: () -> Unit = { navController.popBackStack() }
) {
    val viewModel: ReportViewModel = viewModel()
    var selectedPeriod by remember { mutableStateOf("Daily") }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val reportData by viewModel.reportData.collectAsState()
    val currencyFormatter = remember {
        object : NumberFormat() {
            private val decimalFormat = java.text.DecimalFormat("#,##0.00")

            override fun format(number: Double, toAppendTo: StringBuffer, pos: java.text.FieldPosition): StringBuffer {
                return toAppendTo.append("RM ").append(decimalFormat.format(number))
            }

            override fun format(number: Long, toAppendTo: StringBuffer, pos: java.text.FieldPosition): StringBuffer {
                return toAppendTo.append("RM ").append(decimalFormat.format(number))
            }

            override fun parse(source: String, parsePosition: java.text.ParsePosition): Number? {
                return null
            }
        }
    }

    val periods = listOf("Daily", "Weekly", "Monthly", "Yearly")

    // Load data when period or date changes
    LaunchedEffect(selectedPeriod, selectedDate.timeInMillis) {
        viewModel.loadRevenueData(selectedPeriod, selectedDate)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Revenue Report",
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                navController.navigate(CanteenScreen.OrdersAnalyticsScreen.name)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "Orders Analytics",
                                tint = AccentOrange,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = AccentOrange,
                            modifier = Modifier
                                .size(12.dp)
                                .offset(x = (-8).dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DarkBackground)
        ) {
            when (val state = reportData) {
                is UiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AccentOrange
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
                            tint = Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            color = Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadRevenueData(selectedPeriod, selectedDate) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentOrange
                            )
                        ) {
                            Text("Retry", color = TextPrimary)
                        }
                    }
                }
                is UiState.Success -> {
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
                                    onClick = { selectedPeriod = period },
                                    label = {
                                        Text(
                                            period,
                                            fontSize = 13.sp
                                        )
                                    },
                                    enabled = true,
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = CardBackground,
                                        selectedContainerColor = AccentOrange,
                                        labelColor = TextSecondary,
                                        selectedLabelColor = DarkBackground
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedPeriod == period,
                                        borderColor = Color.Transparent,
                                        selectedBorderColor = Color.Transparent
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Date Navigation
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = CardBackground
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Previous button
                                IconButton(
                                    onClick = {
                                        selectedDate = Calendar.getInstance().apply {
                                            time = selectedDate.time
                                            when (selectedPeriod) {
                                                "Daily" -> add(Calendar.DAY_OF_YEAR, -1)
                                                "Weekly" -> add(Calendar.WEEK_OF_YEAR, -1)
                                                "Monthly" -> add(Calendar.MONTH, -1)
                                                "Yearly" -> add(Calendar.YEAR, -1)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronLeft,
                                        contentDescription = "Previous",
                                        tint = AccentOrange
                                    )
                                }

                                // Date display and calendar button
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(onClick = { showDatePicker = true }) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = "Select Date",
                                            tint = AccentOrange
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
                                        color = TextPrimary
                                    )
                                }

                                // Next button
                                IconButton(
                                    onClick = {
                                        selectedDate = Calendar.getInstance().apply {
                                            time = selectedDate.time
                                            when (selectedPeriod) {
                                                "Daily" -> add(Calendar.DAY_OF_YEAR, 1)
                                                "Weekly" -> add(Calendar.WEEK_OF_YEAR, 1)
                                                "Monthly" -> add(Calendar.MONTH, 1)
                                                "Yearly" -> add(Calendar.YEAR, 1)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Next",
                                        tint = AccentOrange
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Metrics Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricCard(
                                title = "Total Revenue",
                                value = currencyFormatter.format(state.data.totalRevenue),
                                change = String.format("%.1f%%", state.data.revenueChange),
                                isPositive = state.data.isPositiveChange,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Average",
                                value = currencyFormatter.format(state.data.average),
                                subtitle = state.data.averageSubtitle,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Charts Section
                        if (state.data.trendData.isNotEmpty()) {
                            // Chart 1: Revenue Trend
                            ChartCard(
                                title = "Revenue Trend"
                            ) {
                                LineChart(
                                    dataPoints = state.data.trendData,
                                    labels = state.data.trendLabels,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Chart 2: Payment Methods Breakdown
                            if (state.data.paymentMethodData.isNotEmpty()) {
                                ChartCard(
                                    title = "Payment Methods"
                                ) {
                                    PaymentMethodChart(
                                        data = state.data.paymentMethodData,
                                        currencyFormatter = currencyFormatter,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Chart 3: Average Transaction Value
                            ChartCard(
                                title = "Average Transaction Value"
                            ) {
                                BarChart(
                                    dataPoints = state.data.averageTransactionData,
                                    labels = state.data.trendLabels,
                                    currencyFormatter = currencyFormatter,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                )
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = CardBackground
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No data available for this period",
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
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
                            Text("OK", color = AccentOrange)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                    },
                    colors = DatePickerDefaults.colors(
                        containerColor = CardBackground
                    )
                ) {
                    DatePicker(
                        state = datePickerState,
                        colors = DatePickerDefaults.colors(
                            containerColor = CardBackground,
                            titleContentColor = TextPrimary,
                            headlineContentColor = TextPrimary,
                            weekdayContentColor = TextSecondary,
                            subheadContentColor = TextPrimary,
                            yearContentColor = TextPrimary,
                            currentYearContentColor = AccentOrange,
                            selectedYearContentColor = Color.White,
                            selectedYearContainerColor = AccentOrange,
                            dayContentColor = TextPrimary,
                            selectedDayContentColor = Color.White,
                            selectedDayContainerColor = AccentOrange,
                            todayContentColor = AccentOrange,
                            todayDateBorderColor = AccentOrange,
                            dateTextFieldColors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Black,
                                unfocusedContainerColor = Color.Black,
                                cursorColor = AccentOrange,
                                focusedIndicatorColor = AccentOrange,
                                unfocusedIndicatorColor = TextSecondary
                            )

                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    change: String? = null,
    isPositive: Boolean = true,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                color = TextSecondary
            )
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (change != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isPositive) Color(0xFF4CAF50) else Color(0xFFEF4444)
                    )
                    Text(
                        text = change,
                        fontSize = 13.sp,
                        color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFEF4444),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun ChartCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun LineChart(
    dataPoints: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.padding(vertical = 8.dp)) {
        if (dataPoints.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height
        val padding = 40f
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        val maxValue = dataPoints.maxOrNull() ?: 1f
        val minValue = dataPoints.minOrNull() ?: 0f
        val range = if (maxValue - minValue > 0) maxValue - minValue else 1f

        // Draw grid lines
        for (i in 0..4) {
            val y = padding + (chartHeight * i / 4)
            drawLine(
                color = Color.Gray.copy(alpha = 0.15f),
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1f
            )
        }

        // Draw line chart
        val path = Path()
        dataPoints.forEachIndexed { index, value ->
            val x = padding + (chartWidth * index / (dataPoints.size - 1).coerceAtLeast(1))
            val y = padding + chartHeight - ((value - minValue) / range * chartHeight)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }

            // Draw points
            drawCircle(
                color = AccentOrange,
                radius = 6f,
                center = Offset(x, y)
            )
        }

        drawPath(
            path = path,
            color = AccentOrange,
            style = Stroke(width = 3f)
        )

        // Draw labels
        val labelStep = (labels.size / 6).coerceAtLeast(1)
        labels.forEachIndexed { index, label ->
            if (index % labelStep == 0 || index == labels.size - 1) {
                val x = padding + (chartWidth * index / (labels.size - 1).coerceAtLeast(1))
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    x,
                    height - padding / 4,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#B0B0B0")
                        textSize = 24f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }
}

@Composable
fun PaymentMethodChart(
    data: Map<String, Double>,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        AccentOrange,
        AccentYellow,
        Color(0xFF10B981),
        Color(0xFF6366F1),
        Color(0xFF8B5CF6),
        Color(0xFF06B6D4)
    )

    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val total = data.values.sum()
        if (total == 0.0) return@Canvas

        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = minOf(centerX, centerY) * 0.6f

        var startAngle = -90f
        data.entries.forEachIndexed { index, (method, amount) ->
            val sweepAngle = ((amount / total) * 360).toFloat()
            val color = colors[index % colors.size]

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2)
            )

            startAngle += sweepAngle
        }

        // Draw legend
        var legendY = 20f
        data.entries.forEachIndexed { index, (method, amount) ->
            val color = colors[index % colors.size]
            val percentage = (amount / total * 100).toInt()

            drawRect(
                color = color,
                topLeft = Offset(size.width - 180f, legendY),
                size = Size(20f, 20f)
            )

            drawContext.canvas.nativeCanvas.drawText(
                "$method ($percentage%)",
                size.width - 150f,
                legendY + 15f,
                android.graphics.Paint().apply {
                    this.color = android.graphics.Color.parseColor("#FFFFFF")
                    textSize = 32f
                }
            )

            legendY += 35f
        }
    }
}

@Composable
fun BarChart(
    dataPoints: List<Float>,
    labels: List<String>,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.padding(vertical = 8.dp)) {
        if (dataPoints.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height
        val padding = 40f
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        val maxValue = dataPoints.maxOrNull() ?: 1f
        val barWidth = chartWidth / dataPoints.size * 0.7f
        val spacing = chartWidth / dataPoints.size * 0.3f

        dataPoints.forEachIndexed { index, value ->
            val barHeight = if (maxValue > 0) (value / maxValue) * chartHeight else 0f
            val x = padding + (index * (barWidth + spacing))
            val y = padding + chartHeight - barHeight

            drawRect(
                color = AccentOrange,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight)
            )
        }

        val labelStep = (labels.size / 6).coerceAtLeast(1)
        labels.forEachIndexed { index, label ->
            if (index % labelStep == 0 || index == labels.size - 1) {
                val x = padding + (index * (barWidth + spacing)) + barWidth / 2
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    x,
                    height - padding / 4,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#B0B0B0")
                        textSize = 24f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }
}