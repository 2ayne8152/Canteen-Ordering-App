package com.example.canteen.ui.screens.reporting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Mock Data Models
data class ReportTab(
    val title: String,
    val icon: ImageVector
)

data class MockReportData(
    val totalSales: String,
    val salesChange: String,
    val isPositiveChange: Boolean,
    val average: String,
    val averageSubtitle: String,
    val trendData: List<Float>,
    val trendLabels: List<String>,
    val volumeData: List<Float>
)

// Mock Data Provider
object MockReportDataProvider {
    fun getSalesData(period: String): MockReportData {
        return when (period) {
            "Daily" -> MockReportData(
                totalSales = "$37,600",
                salesChange = "+12.5%",
                isPositiveChange = true,
                average = "$5,371",
                averageSubtitle = "Per period",
                trendData = listOf(4000f, 3800f, 5000f, 4500f, 6500f, 8000f, 6000f),
                trendLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
                volumeData = listOf(3000f, 4000f, 5500f, 6800f, 7200f, 6500f, 5000f)
            )
            "Weekly" -> MockReportData(
                totalSales = "$158,400",
                salesChange = "+18.3%",
                isPositiveChange = true,
                average = "$22,628",
                averageSubtitle = "Per week",
                trendData = listOf(18000f, 20000f, 22000f, 25000f, 28000f, 24000f, 21000f),
                trendLabels = listOf("W1", "W2", "W3", "W4", "W5", "W6", "W7"),
                volumeData = listOf(15000f, 18000f, 22000f, 26000f, 28000f, 24000f, 19000f)
            )
            "Monthly" -> MockReportData(
                totalSales = "$654,200",
                salesChange = "+24.7%",
                isPositiveChange = true,
                average = "$54,516",
                averageSubtitle = "Per month",
                trendData = listOf(45000f, 48000f, 52000f, 58000f, 62000f, 68000f, 75000f, 72000f, 68000f, 64000f, 60000f, 58000f),
                trendLabels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"),
                volumeData = listOf(42000f, 46000f, 50000f, 56000f, 60000f, 66000f, 72000f, 70000f, 66000f, 62000f, 58000f, 56000f)
            )
            "Yearly" -> MockReportData(
                totalSales = "$2,458,900",
                salesChange = "+31.2%",
                isPositiveChange = true,
                average = "$204,908",
                averageSubtitle = "Per year",
                trendData = listOf(180000f, 195000f, 210000f, 235000f, 268000f),
                trendLabels = listOf("2020", "2021", "2022", "2023", "2024"),
                volumeData = listOf(175000f, 190000f, 205000f, 230000f, 265000f)
            )
            else -> getSalesData("Daily")
        }
    }

    fun getProfitData(period: String): MockReportData {
        return when (period) {
            "Daily" -> MockReportData(
                totalSales = "$18,450",
                salesChange = "+15.2%",
                isPositiveChange = true,
                average = "$2,635",
                averageSubtitle = "Per period",
                trendData = listOf(2000f, 1900f, 2500f, 2200f, 3200f, 4000f, 2650f),
                trendLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
                volumeData = listOf(1800f, 2100f, 2600f, 3000f, 3400f, 3100f, 2400f)
            )
            "Weekly" -> MockReportData(
                totalSales = "$78,300",
                salesChange = "+21.4%",
                isPositiveChange = true,
                average = "$11,185",
                averageSubtitle = "Per week",
                trendData = listOf(9000f, 10000f, 11000f, 12500f, 14000f, 12000f, 9800f),
                trendLabels = listOf("W1", "W2", "W3", "W4", "W5", "W6", "W7"),
                volumeData = listOf(7500f, 9000f, 11000f, 13000f, 14000f, 12000f, 9500f)
            )
            "Monthly" -> MockReportData(
                totalSales = "$324,800",
                salesChange = "+28.3%",
                isPositiveChange = true,
                average = "$27,066",
                averageSubtitle = "Per month",
                trendData = listOf(22000f, 24000f, 26000f, 29000f, 31000f, 34000f, 37500f, 36000f, 34000f, 32000f, 30000f, 29300f),
                trendLabels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"),
                volumeData = listOf(21000f, 23000f, 25000f, 28000f, 30000f, 33000f, 36000f, 35000f, 33000f, 31000f, 29000f, 28800f)
            )
            "Yearly" -> MockReportData(
                totalSales = "$1,215,600",
                salesChange = "+35.8%",
                isPositiveChange = true,
                average = "$101,300",
                averageSubtitle = "Per year",
                trendData = listOf(90000f, 97500f, 105000f, 117500f, 134000f),
                trendLabels = listOf("2020", "2021", "2022", "2023", "2024"),
                volumeData = listOf(87500f, 95000f, 102500f, 115000f, 132500f)
            )
            else -> getProfitData("Daily")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(onBack: () -> Unit = {}) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedPeriod by remember { mutableStateOf("Daily") }

    val tabs = listOf(
        ReportTab("Profit Summary", Icons.Default.AccountBalance),
        ReportTab("Sales Report", Icons.Default.TrendingUp)
    )

    val periods = listOf("Daily", "Weekly", "Monthly", "Yearly")

    // Get mock data based on selected tab and period
    val currentData = remember(selectedTabIndex, selectedPeriod) {
        when (selectedTabIndex) {
            0 -> MockReportDataProvider.getProfitData(selectedPeriod)
            1 -> MockReportDataProvider.getSalesData(selectedPeriod)
            else -> MockReportDataProvider.getProfitData(selectedPeriod)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Reports",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp,
                indicator = { },
                divider = { }
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (selectedTabIndex == index)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        Color.Transparent
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(18.dp),
                                tint = if (selectedTabIndex == index)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab.title,
                                color = if (selectedTabIndex == index)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp,
                                fontWeight = if (selectedTabIndex == index)
                                    FontWeight.SemiBold
                                else
                                    FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
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
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Metrics Cards with mock data
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = if (selectedTabIndex == 0) "Total Profit" else "Total Sales",
                        value = currentData.totalSales,
                        change = currentData.salesChange,
                        isPositive = currentData.isPositiveChange,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Average",
                        value = currentData.average,
                        subtitle = currentData.averageSubtitle,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Trend Chart with mock data
                ChartCard(
                    title = if (selectedTabIndex == 0) "Profit Trend" else "Sales Trend"
                ) {
                    LineChart(
                        dataPoints = currentData.trendData,
                        labels = currentData.trendLabels,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Volume Chart with mock data
                ChartCard(
                    title = if (selectedTabIndex == 0) "Profit Volume" else "Sales Volume"
                ) {
                    AreaChart(
                        dataPoints = currentData.volumeData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
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
                        tint = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                    Text(
                        text = change,
                        fontSize = 13.sp,
                        color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
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
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier.padding(vertical = 8.dp)) {
        val width = size.width
        val height = size.height
        val padding = 40f
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        val maxValue = dataPoints.maxOrNull() ?: 1f
        val minValue = dataPoints.minOrNull() ?: 0f
        val range = maxValue - minValue

        // Draw grid lines
        for (i in 0..4) {
            val y = padding + (chartHeight * i / 4)
            drawLine(
                color = Color.Gray.copy(alpha = 0.2f),
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1f
            )
        }

        // Draw line chart
        val path = Path()
        dataPoints.forEachIndexed { index, value ->
            val x = padding + (chartWidth * index / (dataPoints.size - 1))
            val y = padding + chartHeight - ((value - minValue) / range * chartHeight)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }

            // Draw points
            drawCircle(
                color = primaryColor,
                radius = 6f,
                center = Offset(x, y)
            )
        }

        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 3f)
        )

        // Draw labels
        labels.forEachIndexed { index, label ->
            val x = padding + (chartWidth * index / (labels.size - 1))
            drawContext.canvas.nativeCanvas.drawText(
                label,
                x,
                height - padding / 4,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

@Composable
fun AreaChart(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier.padding(vertical = 8.dp)) {
        val width = size.width
        val height = size.height
        val padding = 40f
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        val maxValue = dataPoints.maxOrNull() ?: 1f
        val minValue = 0f
        val range = maxValue - minValue

        // Create area path
        val path = Path()
        dataPoints.forEachIndexed { index, value ->
            val x = padding + (chartWidth * index / (dataPoints.size - 1))
            val y = padding + chartHeight - ((value - minValue) / range * chartHeight)

            if (index == 0) {
                path.moveTo(x, padding + chartHeight)
                path.lineTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        // Complete the area path
        path.lineTo(width - padding, padding + chartHeight)
        path.close()

        // Draw filled area
        drawPath(
            path = path,
            color = primaryColor.copy(alpha = 0.3f)
        )

        // Draw line on top
        val linePath = Path()
        dataPoints.forEachIndexed { index, value ->
            val x = padding + (chartWidth * index / (dataPoints.size - 1))
            val y = padding + chartHeight - ((value - minValue) / range * chartHeight)

            if (index == 0) {
                linePath.moveTo(x, y)
            } else {
                linePath.lineTo(x, y)
            }
        }

        drawPath(
            path = linePath,
            color = primaryColor,
            style = Stroke(width = 3f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReportScreenPreview() {
    MaterialTheme {
        ReportScreen()
    }
}