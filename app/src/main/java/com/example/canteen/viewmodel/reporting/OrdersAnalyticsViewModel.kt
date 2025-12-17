package com.example.canteen.viewmodel.reporting

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canteen.data.Order
import com.example.canteen.data.Receipt
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

data class MenuItemAnalytics(
    val menuItemId: String,
    val menuItemName: String,
    val totalOrders: Int,
    val totalQuantity: Int,
    val totalRevenue: Double,
    val averagePrice: Double,
    val percentage: Float
)

data class OrdersAnalyticsData(
    val totalOrders: Int,
    val totalItems: Int,
    val totalRevenue: Double,
    val averageOrderValue: Double,
    val topSellingItems: List<MenuItemAnalytics>,
    val categoryBreakdown: Map<String, Double>,
    val orderTrend: List<Float>,
    val orderTrendLabels: List<String>
)

class OrdersAnalyticsViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _analyticsData = MutableStateFlow<UiState<OrdersAnalyticsData>>(UiState.Loading)
    val analyticsData: StateFlow<UiState<OrdersAnalyticsData>> = _analyticsData.asStateFlow()

    fun loadOrdersAnalytics(period: String, selectedDate: Calendar = Calendar.getInstance()) {
        viewModelScope.launch {
            _analyticsData.value = UiState.Loading

            try {
                val orders = fetchOrders()
                val receipts = fetchReceipts()

                Log.d("OrdersAnalytics", "Fetched ${orders.size} orders")
                Log.d("OrdersAnalytics", "Period: $period, Selected Date: ${selectedDate.time}")

                val data = calculateAnalytics(orders, receipts, period, selectedDate)
                _analyticsData.value = UiState.Success(data)
            } catch (e: Exception) {
                Log.e("OrdersAnalytics", "Error loading analytics", e)
                e.printStackTrace()
                _analyticsData.value = UiState.Error(e.message ?: "Failed to load analytics")
            }
        }
    }

    private suspend fun fetchOrders(): List<Order> {
        return try {
            val snapshot = firestore.collection("orders")
                .get()
                .await()

            Log.d("OrdersAnalytics", "Total documents in orders collection: ${snapshot.documents.size}")

            val orders = snapshot.documents.mapNotNull { doc ->
                try {
                    Log.d("OrdersAnalytics", "Processing document: ${doc.id}")
                    Log.d("OrdersAnalytics", "Document data: ${doc.data}")

                    val order = doc.toObject(Order::class.java)?.copy(orderId = doc.id)

                    if (order != null) {
                        Log.d("OrdersAnalytics", "Successfully parsed order: ${doc.id}")
                        Log.d("OrdersAnalytics", "  - orderId after copy: ${order.orderId}")
                        Log.d("OrdersAnalytics", "  - createdAt: ${order.createdAt}")
                        Log.d("OrdersAnalytics", "  - createdAt.toDate(): ${order.createdAt.toDate()}")
                        Log.d("OrdersAnalytics", "  - totalAmount: ${order.totalAmount}")
                        Log.d("OrdersAnalytics", "  - items count: ${order.items.size}")
                    } else {
                        Log.w("OrdersAnalytics", "Order is null after parsing: ${doc.id}")
                    }

                    order
                } catch (e: Exception) {
                    Log.e("OrdersAnalytics", "Error parsing order ${doc.id}: ${e.message}", e)
                    e.printStackTrace()
                    null
                }
            }

            Log.d("OrdersAnalytics", "Successfully fetched ${orders.size} orders")
            orders
        } catch (e: Exception) {
            Log.e("OrdersAnalytics", "Failed to fetch orders: ${e.message}", e)
            e.printStackTrace()
            throw Exception("Failed to fetch orders: ${e.message}")
        }
    }

    private suspend fun fetchReceipts(): List<Receipt> {
        return try {
            val snapshot = firestore.collection("receipt")
                .get()
                .await()

            Log.d("OrdersAnalytics", "Total receipts: ${snapshot.documents.size}")

            snapshot.documents.mapNotNull { doc ->
                try {
                    val receipt = doc.toObject(Receipt::class.java)
                    Log.d("OrdersAnalytics", "Fetched receipt for order: ${receipt?.orderId}")
                    receipt
                } catch (e: Exception) {
                    Log.e("OrdersAnalytics", "Error parsing receipt ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("OrdersAnalytics", "Failed to fetch receipts", e)
            emptyList()
        }
    }

    private fun calculateAnalytics(
        orders: List<Order>,
        receipts: List<Receipt>,
        period: String,
        selectedDate: Calendar
    ): OrdersAnalyticsData {
        Log.d("OrdersAnalytics", "Calculating analytics for ${orders.size} orders")

        // Filter orders by period using createdAt or receipt payment date
        val validOrderIds = mutableSetOf<String>()

        // Check orders with createdAt dates
        orders.forEach { order ->
            try {
                Log.d("OrdersAnalytics", "Checking order: ${order.orderId}")
                val orderDate = order.createdAt.toDate()
                Log.d("OrdersAnalytics", "  Order date: $orderDate")
                Log.d("OrdersAnalytics", "  Selected date: ${selectedDate.time}")
                Log.d("OrdersAnalytics", "  Period: $period")

                val isValid = isWithinPeriod(orderDate, selectedDate.time, period)
                Log.d("OrdersAnalytics", "  Is valid: $isValid")

                if (isValid) {
                    validOrderIds.add(order.orderId)
                    Log.d("OrdersAnalytics", "  ✓ Added to valid orders")
                } else {
                    Log.d("OrdersAnalytics", "  ✗ Filtered out")
                }
            } catch (e: Exception) {
                Log.e("OrdersAnalytics", "Error processing order ${order.orderId}: ${e.message}", e)
                e.printStackTrace()
            }
        }

        // Also include orders that have receipts in this period
        receipts.forEach { receipt ->
            try {
                val receiptDate = Date(receipt.payment_Date)
                Log.d("OrdersAnalytics", "Checking receipt for order: ${receipt.orderId}, date: $receiptDate")
                if (isWithinPeriod(receiptDate, selectedDate.time, period)) {
                    validOrderIds.add(receipt.orderId)
                    Log.d("OrdersAnalytics", "  ✓ Added order from receipt")
                }
            } catch (e: Exception) {
                Log.e("OrdersAnalytics", "Error processing receipt", e)
            }
        }

        Log.d("OrdersAnalytics", "Valid order IDs: ${validOrderIds.size} - $validOrderIds")

        val filteredOrders = orders.filter { it.orderId in validOrderIds }

        Log.d("OrdersAnalytics", "Filtered orders: ${filteredOrders.size}")
        filteredOrders.forEach { order ->
            Log.d("OrdersAnalytics", "  Filtered order ID: ${order.orderId}")
        }

        // Calculate totals
        val totalOrders = filteredOrders.size
        val allItems = filteredOrders.flatMap { it.items }
        val totalItems = allItems.sumOf { it.quantity }
        val totalRevenue = filteredOrders.sumOf { it.totalAmount }
        val averageOrderValue = if (totalOrders > 0) totalRevenue / totalOrders else 0.0

        // Calculate menu item analytics
        val itemAnalyticsMap = mutableMapOf<String, MutableList<Pair<Order, com.example.canteen.data.CartItem>>>()
        filteredOrders.forEach { order ->
            order.items.forEach { cartItem ->
                itemAnalyticsMap.getOrPut(cartItem.menuItem.id) { mutableListOf() }
                    .add(Pair(order, cartItem))
            }
        }

        val menuItemAnalytics = itemAnalyticsMap.map { (menuItemId, orderItemPairs) ->
            val totalQty = orderItemPairs.sumOf { it.second.quantity }
            val totalRev = orderItemPairs.sumOf { it.second.totalPrice }
            val uniqueOrders = orderItemPairs.map { it.first.orderId }.distinct().size

            MenuItemAnalytics(
                menuItemId = menuItemId,
                menuItemName = orderItemPairs.firstOrNull()?.second?.menuItem?.name ?: "Unknown Item",
                totalOrders = uniqueOrders,
                totalQuantity = totalQty,
                totalRevenue = totalRev,
                averagePrice = if (totalQty > 0) totalRev / totalQty else 0.0,
                percentage = if (totalRevenue > 0) ((totalRev / totalRevenue) * 100).toFloat() else 0f
            )
        }.sortedByDescending { it.totalRevenue }

        // Calculate category breakdown
        val categoryBreakdown = mutableMapOf<String, Double>()
        filteredOrders.forEach { order ->
            order.items.forEach { cartItem ->
                val categoryName = cartItem.menuItem.categoryId.ifBlank { "Uncategorized" }
                val revenue = cartItem.totalPrice
                categoryBreakdown[categoryName] = categoryBreakdown.getOrDefault(categoryName, 0.0) + revenue
            }
        }

        // Calculate order trend
        val (trendData, trendLabels) = calculateOrderTrend(filteredOrders, period, selectedDate)

        return OrdersAnalyticsData(
            totalOrders = totalOrders,
            totalItems = totalItems,
            totalRevenue = totalRevenue,
            averageOrderValue = averageOrderValue,
            topSellingItems = menuItemAnalytics.take(10),
            categoryBreakdown = categoryBreakdown,
            orderTrend = trendData,
            orderTrendLabels = trendLabels
        )
    }

    private fun isWithinPeriod(date: Date, selectedDate: Date, period: String): Boolean {
        Log.d("OrdersAnalytics", "isWithinPeriod called - date: $date, selectedDate: $selectedDate, period: $period")

        // Create calendars and normalize times for consistent comparison
        val selectedCal = Calendar.getInstance().apply {
            time = selectedDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val dateCal = Calendar.getInstance().apply {
            time = date
        }

        // Create a normalized version for day comparison
        val dateCalNormalized = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val result = when (period) {
            "Daily" -> {
                val yearMatch = selectedCal.get(Calendar.YEAR) == dateCalNormalized.get(Calendar.YEAR)
                val dayMatch = selectedCal.get(Calendar.DAY_OF_YEAR) == dateCalNormalized.get(Calendar.DAY_OF_YEAR)
                Log.d("OrdersAnalytics", "  Daily check - yearMatch: $yearMatch, dayMatch: $dayMatch")
                Log.d("OrdersAnalytics", "  Selected: year=${selectedCal.get(Calendar.YEAR)}, day=${selectedCal.get(Calendar.DAY_OF_YEAR)}")
                Log.d("OrdersAnalytics", "  Order: year=${dateCalNormalized.get(Calendar.YEAR)}, day=${dateCalNormalized.get(Calendar.DAY_OF_YEAR)}")
                yearMatch && dayMatch
            }
            "Weekly" -> {
                // Set to start of week
                val weekStart = Calendar.getInstance().apply {
                    time = selectedCal.time
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val weekEnd = Calendar.getInstance().apply {
                    time = weekStart.time
                    add(Calendar.DAY_OF_YEAR, 7)
                }

                !date.before(weekStart.time) && date.before(weekEnd.time)
            }
            "Monthly" -> {
                selectedCal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) &&
                        selectedCal.get(Calendar.MONTH) == dateCal.get(Calendar.MONTH)
            }
            "Yearly" -> {
                selectedCal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR)
            }
            else -> false
        }

        Log.d("OrdersAnalytics", "  Result: $result")
        return result
    }

    private fun calculateOrderTrend(
        orders: List<Order>,
        period: String,
        selectedDate: Calendar
    ): Pair<List<Float>, List<String>> {
        if (orders.isEmpty()) {
            return Pair(emptyList(), emptyList())
        }

        val groupedData = mutableMapOf<String, Int>()
        val sortOrder = mutableMapOf<String, Long>()

        when (period) {
            "Daily" -> {
                for (i in 0..23) {
                    val label = String.format("%02d:00", i)
                    groupedData[label] = 0
                    sortOrder[label] = i.toLong()
                }

                orders.forEach { order ->
                    try {
                        val date = order.createdAt.toDate()
                        val cal = Calendar.getInstance().apply { time = date }
                        val hour = cal.get(Calendar.HOUR_OF_DAY)
                        val label = String.format("%02d:00", hour)
                        groupedData[label] = groupedData.getOrDefault(label, 0) + 1
                    } catch (e: Exception) {
                        Log.e("OrdersAnalytics", "Error processing order trend", e)
                    }
                }
            }
            "Weekly" -> {
                val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                daysOfWeek.forEachIndexed { index, day ->
                    groupedData[day] = 0
                    sortOrder[day] = index.toLong()
                }

                orders.forEach { order ->
                    try {
                        val date = order.createdAt.toDate()
                        val cal = Calendar.getInstance().apply { time = date }
                        val dayIndex = cal.get(Calendar.DAY_OF_WEEK) - 1
                        val label = daysOfWeek[dayIndex]
                        groupedData[label] = groupedData.getOrDefault(label, 0) + 1
                    } catch (e: Exception) {
                        Log.e("OrdersAnalytics", "Error processing order trend", e)
                    }
                }
            }
            "Monthly" -> {
                val cal = Calendar.getInstance().apply { time = selectedDate.time }
                val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

                for (day in 1..daysInMonth) {
                    val label = day.toString()
                    groupedData[label] = 0
                    sortOrder[label] = day.toLong()
                }

                orders.forEach { order ->
                    try {
                        val date = order.createdAt.toDate()
                        val orderCal = Calendar.getInstance().apply { time = date }
                        val day = orderCal.get(Calendar.DAY_OF_MONTH)
                        val label = day.toString()
                        groupedData[label] = groupedData.getOrDefault(label, 0) + 1
                    } catch (e: Exception) {
                        Log.e("OrdersAnalytics", "Error processing order trend", e)
                    }
                }
            }
            "Yearly" -> {
                val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                months.forEachIndexed { index, month ->
                    groupedData[month] = 0
                    sortOrder[month] = index.toLong()
                }

                orders.forEach { order ->
                    try {
                        val date = order.createdAt.toDate()
                        val cal = Calendar.getInstance().apply { time = date }
                        val monthIndex = cal.get(Calendar.MONTH)
                        val label = months[monthIndex]
                        groupedData[label] = groupedData.getOrDefault(label, 0) + 1
                    } catch (e: Exception) {
                        Log.e("OrdersAnalytics", "Error processing order trend", e)
                    }
                }
            }
        }

        val sortedEntries = groupedData.entries.sortedBy { sortOrder[it.key] ?: 0L }
        val trendData = sortedEntries.map { it.value.toFloat() }
        val labels = sortedEntries.map { it.key }

        return Pair(trendData, labels)
    }
}