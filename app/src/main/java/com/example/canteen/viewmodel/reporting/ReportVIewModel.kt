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

    fun loadRevenueData(period: String, selectedDate: Calendar = Calendar.getInstance()) {
        viewModelScope.launch {
            _reportData.value = UiState.Loading

            try {
                // Fetch receipts from Firebase
                val receipts = fetchReceiptsFromFirebase()
                val data = calculateRevenueData(receipts, period, selectedDate)
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

    private fun calculateRevenueData(receipts: List<ReceiptWithTimestamp>, period: String, selectedDate: Calendar): RevenueReportData {
        // Filter receipts based on period and selected date
        val filteredReceipts = receipts.filter { receipt ->
            isWithinPeriod(receipt.paymentDate, selectedDate.time, period)
        }

        // Calculate total revenue
        val totalRevenue = filteredReceipts.sumOf { it.payAmount }

        // Calculate previous period revenue for comparison
        val previousPeriodReceipts = receipts.filter { receipt ->
            isWithinPreviousPeriod(receipt.paymentDate, selectedDate.time, period)
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

    private fun isWithinPeriod(date: Date, selectedDate: Date, period: String): Boolean {
        val cal = Calendar.getInstance()
        cal.time = selectedDate

        when (period) {
            "Daily" -> {
                // Check if date is on the same day as selectedDate
                val dateCal = Calendar.getInstance().apply { time = date }
                return cal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) &&
                        cal.get(Calendar.DAY_OF_YEAR) == dateCal.get(Calendar.DAY_OF_YEAR)
            }
            "Weekly" -> {
                // Check if date is in the same week as selectedDate
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                val weekStart = cal.time
                cal.add(Calendar.DAY_OF_YEAR, 6)
                val weekEnd = cal.time
                return (date.after(weekStart) || date == weekStart) &&
                        (date.before(weekEnd) || date == weekEnd)
            }
            "Monthly" -> {
                // Check if date is in the same month as selectedDate
                val dateCal = Calendar.getInstance().apply { time = date }
                return cal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) &&
                        cal.get(Calendar.MONTH) == dateCal.get(Calendar.MONTH)
            }
            "Yearly" -> {
                // Check if date is in the same year as selectedDate
                val dateCal = Calendar.getInstance().apply { time = date }
                return cal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR)
            }
        }

        return false
    }

    private fun isWithinPreviousPeriod(date: Date, selectedDate: Date, period: String): Boolean {
        val cal = Calendar.getInstance()
        cal.time = selectedDate

        when (period) {
            "Daily" -> {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val prevDay = cal.time
                val dateCal = Calendar.getInstance().apply { time = date }
                val prevCal = Calendar.getInstance().apply { time = prevDay }
                return prevCal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) &&
                        prevCal.get(Calendar.DAY_OF_YEAR) == dateCal.get(Calendar.DAY_OF_YEAR)
            }
            "Weekly" -> {
                cal.add(Calendar.WEEK_OF_YEAR, -1)
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                val weekStart = cal.time
                cal.add(Calendar.DAY_OF_YEAR, 6)
                val weekEnd = cal.time
                return (date.after(weekStart) || date == weekStart) &&
                        (date.before(weekEnd) || date == weekEnd)
            }
            "Monthly" -> {
                cal.add(Calendar.MONTH, -1)
                val dateCal = Calendar.getInstance().apply { time = date }
                return cal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) &&
                        cal.get(Calendar.MONTH) == dateCal.get(Calendar.MONTH)
            }
            "Yearly" -> {
                cal.add(Calendar.YEAR, -1)
                val dateCal = Calendar.getInstance().apply { time = date }
                return cal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR)
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