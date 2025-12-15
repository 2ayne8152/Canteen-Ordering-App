package com.example.canteen.viewmodel.reporting

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val paymentMethodData: Map<String, Double>,
    val averageTransactionData: List<Float>,
    val transactionCountData: List<Int>
)

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

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
                val receipts = fetchReceiptsFromFirebase()
                Log.d("ReportViewModel", "Fetched ${receipts.size} receipts")

                val data = calculateRevenueData(receipts, period, selectedDate)
                _reportData.value = UiState.Success(data)
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Error loading data", e)
                _reportData.value = UiState.Error(e.message ?: "Failed to load revenue data")
            }
        }
    }

    private suspend fun fetchReceiptsFromFirebase(): List<ReceiptWithTimestamp> {
        return try {
            val snapshot = firestore.collection("receipt")
                .get()
                .await()

            Log.d("ReportViewModel", "Total documents in receipt collection: ${snapshot.documents.size}")

            snapshot.documents.mapNotNull { document ->
                try {
                    var date: Date? = null

                    // First try to get as Long (milliseconds) - this is what your DB stores
                    val millis = document.getLong("payment_Date")
                        ?: document.getLong("Payment_Date")
                        ?: document.getLong("paymentDate")

                    if (millis != null) {
                        date = Date(millis)
                        Log.d("ReportViewModel", "Parsed date from millis: $millis -> ${date}")
                    } else {
                        // Fallback: Try to get timestamp (Firestore Timestamp object)
                        val timestamp = document.getTimestamp("payment_Date")
                            ?: document.getTimestamp("Payment_Date")
                            ?: document.getTimestamp("paymentDate")

                        if (timestamp != null) {
                            date = timestamp.toDate()
                        }
                    }

                    val payAmount = document.getDouble("pay_Amount")
                        ?: document.getDouble("Pay_Amount")
                        ?: document.getDouble("payAmount")
                        ?: 0.0

                    if (date != null && payAmount > 0) {
                        val receipt = ReceiptWithTimestamp(
                            receiptID = document.getString("receiptId")
                                ?: document.getString("ReceiptID")
                                ?: document.getString("receiptID")
                                ?: document.getString("orderId")
                                ?: document.getString("OrderID")
                                ?: document.id,
                            paymentDate = date,
                            payAmount = payAmount,
                            paymentMethod = document.getString("payment_Method")
                                ?: document.getString("Payment_Method")
                                ?: document.getString("paymentMethod")
                                ?: ""
                        )

                        Log.d("ReportViewModel", "Receipt: ${receipt.receiptID}, Amount: ${receipt.payAmount}, Date: ${receipt.paymentDate}")
                        receipt
                    } else {
                        Log.w("ReportViewModel", "Skipped document ${document.id}: date=$date, amount=$payAmount")
                        null
                    }
                } catch (e: Exception) {
                    Log.e("ReportViewModel", "Error parsing document ${document.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("ReportViewModel", "Error fetching receipts", e)
            throw Exception("Failed to fetch receipts: ${e.message}")
        }
    }

    private fun calculateRevenueData(
        receipts: List<ReceiptWithTimestamp>,
        period: String,
        selectedDate: Calendar
    ): RevenueReportData {
        Log.d("ReportViewModel", "Calculating for period: $period, date: ${selectedDate.time}")

        val filteredReceipts = receipts.filter { receipt ->
            isWithinPeriod(receipt.paymentDate, selectedDate.time, period)
        }

        Log.d("ReportViewModel", "Filtered receipts: ${filteredReceipts.size}")

        val totalRevenue = filteredReceipts.sumOf { it.payAmount }
        Log.d("ReportViewModel", "Total revenue: $totalRevenue")

        val previousPeriodReceipts = receipts.filter { receipt ->
            isWithinPreviousPeriod(receipt.paymentDate, selectedDate.time, period)
        }
        val previousRevenue = previousPeriodReceipts.sumOf { it.payAmount }

        val revenueChange = if (previousRevenue > 0) {
            ((totalRevenue - previousRevenue) / previousRevenue) * 100
        } else if (totalRevenue > 0) {
            100.0
        } else {
            0.0
        }

        val (trendData, trendLabels, paymentMethodData, averageTransactionData, transactionCountData) = calculateTrendData(
            filteredReceipts,
            period,
            selectedDate
        )

        val average = if (trendData.isNotEmpty()) {
            trendData.average()
        } else {
            0.0
        }

        val averageSubtitle = when (period) {
            "Daily" -> "Per hour"
            "Weekly" -> "Per day"
            "Monthly" -> "Per day"
            "Yearly" -> "Per month"
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
            paymentMethodData = paymentMethodData,
            averageTransactionData = averageTransactionData,
            transactionCountData = transactionCountData
        )
    }

    private fun isWithinPeriod(date: Date, selectedDate: Date, period: String): Boolean {
        val cal = Calendar.getInstance().apply { time = selectedDate }
        val dateCal = Calendar.getInstance().apply { time = date }

        val result = when (period) {
            "Daily" -> {
                cal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) &&
                        cal.get(Calendar.DAY_OF_YEAR) == dateCal.get(Calendar.DAY_OF_YEAR)
            }
            "Weekly" -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val weekStart = cal.time

                cal.add(Calendar.DAY_OF_YEAR, 7)
                val weekEnd = cal.time

                !date.before(weekStart) && date.before(weekEnd)
            }
            "Monthly" -> {
                cal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) &&
                        cal.get(Calendar.MONTH) == dateCal.get(Calendar.MONTH)
            }
            "Yearly" -> {
                cal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR)
            }
            else -> false
        }

        Log.d("ReportViewModel", "Date ${dateCal.time} within period? $result")
        return result
    }

    private fun isWithinPreviousPeriod(date: Date, selectedDate: Date, period: String): Boolean {
        val cal = Calendar.getInstance().apply { time = selectedDate }

        when (period) {
            "Daily" -> cal.add(Calendar.DAY_OF_YEAR, -1)
            "Weekly" -> cal.add(Calendar.WEEK_OF_YEAR, -1)
            "Monthly" -> cal.add(Calendar.MONTH, -1)
            "Yearly" -> cal.add(Calendar.YEAR, -1)
        }

        return isWithinPeriod(date, cal.time, period)
    }

    private fun calculateTrendData(
        receipts: List<ReceiptWithTimestamp>,
        period: String,
        selectedDate: Calendar
    ): Tuple5<List<Float>, List<String>, Map<String, Double>, List<Float>, List<Int>> {
        if (receipts.isEmpty()) {
            return Tuple5(emptyList(), emptyList(), emptyMap(), emptyList(), emptyList())
        }

        val groupedData = mutableMapOf<String, Double>()
        val groupedVolume = mutableMapOf<String, Int>()
        val sortOrder = mutableMapOf<String, Long>()

        // Calculate payment method breakdown
        val paymentMethodTotals = mutableMapOf<String, Double>()
        receipts.forEach { receipt ->
            val method = receipt.paymentMethod.ifEmpty { "Unknown" }
            paymentMethodTotals[method] = paymentMethodTotals.getOrDefault(method, 0.0) + receipt.payAmount
        }

        when (period) {
            "Daily" -> {
                for (i in 0..23) {
                    val label = String.format("%02d:00", i)
                    groupedData[label] = 0.0
                    groupedVolume[label] = 0
                    sortOrder[label] = i.toLong()
                }

                receipts.forEach { receipt ->
                    val cal = Calendar.getInstance().apply { time = receipt.paymentDate }
                    val hour = cal.get(Calendar.HOUR_OF_DAY)
                    val label = String.format("%02d:00", hour)
                    groupedData[label] = groupedData.getOrDefault(label, 0.0) + receipt.payAmount
                    groupedVolume[label] = groupedVolume.getOrDefault(label, 0) + 1
                }
            }
            "Weekly" -> {
                val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                daysOfWeek.forEachIndexed { index, day ->
                    groupedData[day] = 0.0
                    groupedVolume[day] = 0
                    sortOrder[day] = index.toLong()
                }

                receipts.forEach { receipt ->
                    val cal = Calendar.getInstance().apply { time = receipt.paymentDate }
                    val dayIndex = cal.get(Calendar.DAY_OF_WEEK) - 1
                    val label = daysOfWeek[dayIndex]
                    groupedData[label] = groupedData.getOrDefault(label, 0.0) + receipt.payAmount
                    groupedVolume[label] = groupedVolume.getOrDefault(label, 0) + 1
                }
            }
            "Monthly" -> {
                val cal = Calendar.getInstance().apply { time = selectedDate.time }
                val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

                for (day in 1..daysInMonth) {
                    val label = day.toString()
                    groupedData[label] = 0.0
                    groupedVolume[label] = 0
                    sortOrder[label] = day.toLong()
                }

                receipts.forEach { receipt ->
                    val receiptCal = Calendar.getInstance().apply { time = receipt.paymentDate }
                    val day = receiptCal.get(Calendar.DAY_OF_MONTH)
                    val label = day.toString()
                    groupedData[label] = groupedData.getOrDefault(label, 0.0) + receipt.payAmount
                    groupedVolume[label] = groupedVolume.getOrDefault(label, 0) + 1
                }
            }
            "Yearly" -> {
                val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                months.forEachIndexed { index, month ->
                    groupedData[month] = 0.0
                    groupedVolume[month] = 0
                    sortOrder[month] = index.toLong()
                }

                receipts.forEach { receipt ->
                    val cal = Calendar.getInstance().apply { time = receipt.paymentDate }
                    val monthIndex = cal.get(Calendar.MONTH)
                    val label = months[monthIndex]
                    groupedData[label] = groupedData.getOrDefault(label, 0.0) + receipt.payAmount
                    groupedVolume[label] = groupedVolume.getOrDefault(label, 0) + 1
                }
            }
        }

        val sortedEntries = groupedData.entries.sortedBy { sortOrder[it.key] ?: 0L }
        val trendData = sortedEntries.map { it.value.toFloat() }
        val labels = sortedEntries.map { it.key }

        // Calculate average transaction value per time period
        val averageTransactionData = sortedEntries.map { entry ->
            val volume = groupedVolume[entry.key] ?: 0
            if (volume > 0) (entry.value / volume).toFloat() else 0f
        }

        val transactionCountData = sortedEntries.map { groupedVolume[it.key] ?: 0 }

        return Tuple5(trendData, labels, paymentMethodTotals, averageTransactionData, transactionCountData)
    }
}

// Helper class for returning 5 values
data class Tuple5<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)