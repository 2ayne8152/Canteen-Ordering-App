package com.example.canteen.viewmodel.reporting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canteen.data.Receipt
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class RevenueReportData(
    val totalRevenue: Double,
    val revenueChange: Double,
    val isPositiveChange: Boolean,
    val average: Double,
    val averageSubtitle: String,
    val trendData: List<Float>,
    val trendLabels: List<String>,
    val volumeData: List<Float>
)

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

// Data class to hold receipt with timestamp
data class ReceiptWithTimestamp(
    val receiptID: String,
    val paymentDate: Date,
    val payAmount: Double,
    val paymentMethod: String
)

class ReportViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _reportData = MutableStateFlow<UiState<RevenueReportData>>(UiState.Loading)
    val reportData: StateFlow<UiState<RevenueReportData>> = _reportData.asStateFlow()

    fun loadRevenueData(period: String) {
        viewModelScope.launch {
            _reportData.value = UiState.Loading

            try {
                // Fetch receipts from Firebase
                val receipts = fetchReceiptsFromFirebase()
                val data = calculateRevenueData(receipts, period)
                _reportData.value = UiState.Success(data)
            } catch (e: Exception) {
                _reportData.value = UiState.Error(e.message ?: "Failed to load revenue data")
            }
        }
    }

    private suspend fun fetchReceiptsFromFirebase(): List<ReceiptWithTimestamp> {
        return try {
            val snapshot = firestore.collection("Receipt")
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                try {
                    val timestamp = document.getTimestamp("Payment_Date")
                    val payAmount = document.getDouble("Pay_Amount") ?: 0.0

                    if (timestamp != null && payAmount > 0) {
                        ReceiptWithTimestamp(
                            receiptID = document.getString("ReceiptID") ?: document.id,
                            paymentDate = timestamp.toDate(),
                            payAmount = payAmount,
                            paymentMethod = document.getString("Payment_Method") ?: ""
                        )
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null // Skip invalid documents
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch receipts: ${e.message}")
        }
    }

    private fun calculateRevenueData(receipts: List<ReceiptWithTimestamp>, period: String): RevenueReportData {
        val now = Calendar.getInstance()

        // Filter receipts based on period
        val filteredReceipts = receipts.filter { receipt ->
            isWithinPeriod(receipt.paymentDate, now.time, period)
        }

        // Calculate total revenue
        val totalRevenue = filteredReceipts.sumOf { it.payAmount }

        // Calculate previous period revenue for comparison
        val previousPeriodReceipts = receipts.filter { receipt ->
            isWithinPreviousPeriod(receipt.paymentDate, now.time, period)
        }
        val previousRevenue = previousPeriodReceipts.sumOf { it.payAmount }

        // Calculate percentage change
        val revenueChange = if (previousRevenue > 0) {
            ((totalRevenue - previousRevenue) / previousRevenue) * 100
        } else if (totalRevenue > 0) {
            100.0 // If no previous revenue but current revenue exists, show 100% increase
        } else {
            0.0
        }

        // Group data by time periods and calculate trend
        val (trendData, trendLabels, volumeData) = calculateTrendData(
            filteredReceipts,
            period
        )

        // Calculate average
        val divisor = when (period) {
            "Daily" -> 7
            "Weekly" -> 4
            "Monthly" -> 12
            "Yearly" -> 5
            else -> 1
        }
        val average = if (trendData.isNotEmpty()) {
            trendData.average()
        } else {
            totalRevenue / divisor
        }

        val averageSubtitle = when (period) {
            "Daily" -> "Per day"
            "Weekly" -> "Per week"
            "Monthly" -> "Per month"
            "Yearly" -> "Per year"
            else -> "Per period"
        }

        return RevenueReportData(
            totalRevenue = totalRevenue,
            revenueChange = revenueChange,
            isPositiveChange = revenueChange >= 0,
            average = average,
            averageSubtitle = averageSubtitle,
            trendData = trendData,
            trendLabels = trendLabels,
            volumeData = volumeData
        )
    }

    private fun isWithinPeriod(date: Date, now: Date, period: String): Boolean {
        val cal = Calendar.getInstance()
        cal.time = now

        when (period) {
            "Daily" -> cal.add(Calendar.DAY_OF_YEAR, -7)
            "Weekly" -> cal.add(Calendar.WEEK_OF_YEAR, -7)
            "Monthly" -> cal.add(Calendar.MONTH, -12)
            "Yearly" -> cal.add(Calendar.YEAR, -5)
        }

        return date.after(cal.time) || date == cal.time
    }

    private fun isWithinPreviousPeriod(date: Date, now: Date, period: String): Boolean {
        val cal = Calendar.getInstance()
        cal.time = now

        when (period) {
            "Daily" -> {
                cal.add(Calendar.DAY_OF_YEAR, -14)
                val startDate = cal.time
                cal.add(Calendar.DAY_OF_YEAR, 7)
                val endDate = cal.time
                return date.after(startDate) && date.before(endDate)
            }
            "Weekly" -> {
                cal.add(Calendar.WEEK_OF_YEAR, -14)
                val startDate = cal.time
                cal.add(Calendar.WEEK_OF_YEAR, 7)
                val endDate = cal.time
                return date.after(startDate) && date.before(endDate)
            }
            "Monthly" -> {
                cal.add(Calendar.MONTH, -24)
                val startDate = cal.time
                cal.add(Calendar.MONTH, 12)
                val endDate = cal.time
                return date.after(startDate) && date.before(endDate)
            }
            "Yearly" -> {
                cal.add(Calendar.YEAR, -10)
                val startDate = cal.time
                cal.add(Calendar.YEAR, 5)
                val endDate = cal.time
                return date.after(startDate) && date.before(endDate)
            }
        }
        return false
    }

    private fun calculateTrendData(
        receipts: List<ReceiptWithTimestamp>,
        period: String
    ): Triple<List<Float>, List<String>, List<Float>> {
        if (receipts.isEmpty()) {
            return Triple(emptyList(), emptyList(), emptyList())
        }

        val groupedData = mutableMapOf<String, Double>()
        val labelFormat = when (period) {
            "Daily" -> SimpleDateFormat("EEE", Locale.getDefault()) // Mon, Tue, etc.
            "Weekly" -> SimpleDateFormat("'W'w", Locale.getDefault()) // W1, W2, etc.
            "Monthly" -> SimpleDateFormat("MMM", Locale.getDefault()) // Jan, Feb, etc.
            "Yearly" -> SimpleDateFormat("yyyy", Locale.getDefault()) // 2020, 2021, etc.
            else -> SimpleDateFormat("MMM dd", Locale.getDefault())
        }

        receipts.forEach { receipt ->
            val label = labelFormat.format(receipt.paymentDate)
            groupedData[label] = groupedData.getOrDefault(label, 0.0) + receipt.payAmount
        }

        // Sort by date
        val sortedEntries = groupedData.entries.sortedBy { entry ->
            receipts.find { labelFormat.format(it.paymentDate) == entry.key }?.paymentDate?.time ?: 0L
        }

        val trendData = sortedEntries.map { it.value.toFloat() }
        val labels = sortedEntries.map { it.key }
        val volumeData = trendData.toList()

        return Triple(trendData, labels, volumeData)
    }
}