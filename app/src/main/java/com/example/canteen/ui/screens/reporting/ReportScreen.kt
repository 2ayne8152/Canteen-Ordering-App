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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.util.*
import com.example.canteen.viewmodel.reporting.UiState
import com.example.canteen.viewmodel.reporting.ReportViewModel
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    onBack: () -> Unit = {}
) {
    val viewModel: ReportViewModel = viewModel() // No factory needed - uses Firebase directly
    var selectedPeriod by remember { mutableStateOf("Daily") }
    val reportData by viewModel.reportData.collectAsState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    val periods = listOf("Daily", "Weekly", "Monthly", "Yearly")

    // Load data when period changes
    LaunchedEffect(selectedPeriod) {
        viewModel.loadRevenueData(selectedPeriod)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Revenue Report",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            when (val state = reportData) {
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
                        Button(onClick = { viewModel.loadRevenueData(selectedPeriod) }) {
                            Text("Retry")
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
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
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

                        // Trend Chart
                        if (state.data.trendData.isNotEmpty()) {
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

                            // Volume Chart
                            ChartCard(
                                title = "Revenue Volume"
                            ) {
                                AreaChart(
                                    dataPoints = state.data.volumeData,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                )
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
                                        text = "No data available for this period",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
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
                color = Color.Gray.copy(alpha = 0.2f),
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
            val x = padding + (chartWidth * index / (labels.size - 1).coerceAtLeast(1))
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
        if (dataPoints.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height
        val padding = 40f
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        val maxValue = dataPoints.maxOrNull() ?: 1f
        val minValue = 0f
        val range = if (maxValue > 0) maxValue else 1f

        // Create area path
        val path = Path()
        dataPoints.forEachIndexed { index, value ->
            val x = padding + (chartWidth * index / (dataPoints.size - 1).coerceAtLeast(1))
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
            val x = padding + (chartWidth * index / (dataPoints.size - 1).coerceAtLeast(1))
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