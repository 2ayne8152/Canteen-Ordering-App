package com.example.canteen.ui.screens.reporting

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import androidx.compose.material3.ExperimentalMaterial3Api
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(onBack: () -> Unit = {}) {
    var selectedReportType by remember { mutableStateOf("Sales") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }


    val reportTypes = listOf("Sales", "Item", "Orders", "Revenue")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Report Type Selection
            Text(
                text = "Report Type",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            reportTypes.forEach { type ->
                FilterChip(
                    selected = selectedReportType == type,
                    onClick = { selectedReportType = type },
                    label = { Text(type) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            HorizontalDivider()

            // Date Range Selection
            Text(
                text = "Date Range",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text("Start Date") },
                placeholder = { Text("DD/MM/YYYY") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = { Text("End Date") },
                placeholder = { Text("DD/MM/YYYY") },
                modifier = Modifier.fillMaxWidth()
            )

            // Quick Date Presets
            Text(
                text = "Quick Select",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { setDateRange("today") { s, e ->
                        startDate = s
                        endDate = e
                    }},
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Today")
                }

                OutlinedButton(
                    onClick = { setDateRange("week") { s, e ->
                        startDate = s
                        endDate = e
                    }},
                    modifier = Modifier.weight(1f)
                ) {
                    Text("This Week")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { setDateRange("month") { s, e ->
                        startDate = s
                        endDate = e
                    }},
                    modifier = Modifier.weight(1f)
                ) {
                    Text("This Month")
                }

                OutlinedButton(
                    onClick = { setDateRange("year") { s, e ->
                        startDate = s
                        endDate = e
                    }},
                    modifier = Modifier.weight(1f)
                ) {
                    Text("This Year")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Generate Report Button
            Button(
                onClick = {
                    isLoading = true
                    // TODO: Generate report with Firebase data
                    // For now, just simulate loading
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && startDate.isNotEmpty() && endDate.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isLoading) "Generating..." else "Generate Report")
            }

            // Placeholder for report preview
            if (!isLoading && startDate.isNotEmpty() && endDate.isNotEmpty()) {
                HorizontalDivider()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Report Preview",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Type: $selectedReportType",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Period: $startDate - $endDate",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Firebase integration pending...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

// Helper function to set date ranges
private fun setDateRange(period: String, onDateSet: (String, String) -> Unit) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val calendar = Calendar.getInstance()
    val endDate = dateFormat.format(calendar.time)

    when (period) {
        "today" -> {
            onDateSet(endDate, endDate)
        }
        "week" -> {
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            onDateSet(dateFormat.format(calendar.time), endDate)
        }
        "month" -> {
            calendar.add(Calendar.MONTH, -1)
            onDateSet(dateFormat.format(calendar.time), endDate)
        }
        "year" -> {
            calendar.add(Calendar.YEAR, -1)
            onDateSet(dateFormat.format(calendar.time), endDate)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReportScreenPreview() {
    MaterialTheme {
        ReportScreen()
    }
}