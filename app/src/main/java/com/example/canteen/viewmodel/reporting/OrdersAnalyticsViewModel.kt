package com.example.canteen.viewmodel.reporting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class OrderMenuItem(
    val orderMenuItemId: String,
    val orderId: String,
    val menuItemId: String,
    val quantity: Int,
    val price: Double
)

data class MenuItem(
    val id: String,
    val name: String,
    val categoryId: String,
    val price: Double
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
                val orderMenuItems = fetchOrderMenuItems()
                val menuItems = fetchMenuItems()
                val receipts = fetchReceipts()

                val data = calculateAnalytics(orderMenuItems, menuItems, receipts, period, selectedDate)
                _analyticsData.value = UiState.Success(data)
            } catch (e: Exception) {
                _analyticsData.value = UiState.Error(e.message ?: "Failed to load analytics")
            }
        }
    }

    private suspend fun fetchOrderMenuItems(): List<OrderMenuItem> {
        return try {
            val snapshot = firestore.collection("Order_MenuItem")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    OrderMenuItem(
                        orderMenuItemId = doc.getString("Order_MenuItem_ID")
                            ?: doc.getString("order_MenuItem_ID")
                            ?: doc.id,
                        orderId = doc.getString("OrderID")
                            ?: doc.getString("orderId")
                            ?: "",
                        menuItemId = doc.getString("MenuItem_ID")
                            ?: doc.getString("menuItem_ID")
                            ?: "",
                        quantity = doc.getLong("Quantity")?.toInt()
                            ?: doc.getLong("quantity")?.toInt()
                            ?: 0,
                        price = doc.getDouble("Price")
                            ?: doc.getDouble("price")
                            ?: 0.0
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch order items: ${e.message}")
        }
    }

    private suspend fun fetchMenuItems(): List<MenuItem> {
        return try {
            val snapshot = firestore.collection("MenuItems")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    MenuItem(
                        id = doc.getString("id") ?: doc.id,
                        name = doc.getString("name") ?: "Unknown",
                        categoryId = doc.getString("CategoryID")
                            ?: doc.getString("categoryID")
                            ?: doc.getString("categoryId")
                            ?: "",
                        price = doc.getDouble("price") ?: 0.0
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch menu items: ${e.message}")
        }
    }


    private suspend fun fetchReceipts(): List<ReceiptWithTimestamp> {
        return try {
            val snapshot = firestore.collection("receipt")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    val timestamp = doc.getTimestamp("payment_Date")
                        ?: doc.getTimestamp("Payment_Date")
                        ?: doc.getTimestamp("paymentDate")

                    val orderId = doc.getString("orderId")
                        ?: doc.getString("orderID")
                        ?: doc.getString("OrderID")
                        ?: ""

                    if (timestamp != null) {
                        ReceiptWithTimestamp(
                            receiptID = orderId,
                            paymentDate = timestamp.toDate(),
                            payAmount = doc.getDouble("pay_Amount")
                                ?: doc.getDouble("Pay_Amount")
                                ?: 0.0,
                            paymentMethod = ""
                        )
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun calculateAnalytics(
        orderMenuItems: List<OrderMenuItem>,
        menuItems: List<MenuItem>,
        receipts: List<ReceiptWithTimestamp>,
        period: String,
        selectedDate: Calendar
    ): OrdersAnalyticsData {
        // Filter orders by period
        val validOrderIds = receipts.filter { receipt ->
            isWithinPeriod(receipt.paymentDate, selectedDate.time, period)
        }.map { it.receiptID }.toSet()

        val filteredOrderItems = orderMenuItems.filter { it.orderId in validOrderIds }

        // Calculate totals
        val totalOrders = validOrderIds.size
        val totalItems = filteredOrderItems.sumOf { it.quantity }
        val totalRevenue = filteredOrderItems.sumOf { it.price * it.quantity }
        val averageOrderValue = if (totalOrders > 0) totalRevenue / totalOrders else 0.0

        // Calculate menu item analytics
        val itemAnalyticsMap = mutableMapOf<String, MutableList<OrderMenuItem>>()
        filteredOrderItems.forEach { orderItem ->
            itemAnalyticsMap.getOrPut(orderItem.menuItemId) { mutableListOf() }.add(orderItem)
        }

        val menuItemAnalytics = itemAnalyticsMap.map { (menuItemId, items) ->
            val menuItem = menuItems.find { it.id == menuItemId }
            val totalQty = items.sumOf { it.quantity }
            val totalRev = items.sumOf { it.price * it.quantity }

            MenuItemAnalytics(
                menuItemId = menuItemId,
                menuItemName = menuItem?.name ?: "Unknown Item",
                totalOrders = items.size,
                totalQuantity = totalQty,
                totalRevenue = totalRev,
                averagePrice = if (totalQty > 0) totalRev / totalQty else 0.0,
                percentage = if (totalRevenue > 0) ((totalRev / totalRevenue) * 100).toFloat() else 0f
            )
        }.sortedByDescending { it.totalRevenue }

        // Calculate category breakdown
        val categoryBreakdown = mutableMapOf<String, Double>()
        filteredOrderItems.forEach { orderItem ->
            val menuItem = menuItems.find { it.id == orderItem.menuItemId }
            if (menuItem != null) {
                val categoryName = menuItem.categoryId
                val revenue = orderItem.price * orderItem.quantity
                categoryBreakdown[categoryName] = categoryBreakdown.getOrDefault(categoryName, 0.0) + revenue
            }
        }

        // Calculate order trend
        val (trendData, trendLabels) = calculateOrderTrend(receipts, period, selectedDate)

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
        val cal = Calendar.getInstance().apply { time = selectedDate }
        val dateCal = Calendar.getInstance().apply { time = date }

        return when (period) {
            "Daily" -> {
                cal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) &&
                        cal.get(Calendar.DAY_OF_YEAR) == dateCal.get(Calendar.DAY_OF_YEAR)
            }
            "Weekly" -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
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
    }

    private fun calculateOrderTrend(
        receipts: List<ReceiptWithTimestamp>,
        period: String,
        selectedDate: Calendar
    ): Pair<List<Float>, List<String>> {
        val filteredReceipts = receipts.filter {
            isWithinPeriod(it.paymentDate, selectedDate.time, period)
        }

        if (filteredReceipts.isEmpty()) {
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

                filteredReceipts.forEach { receipt ->
                    val cal = Calendar.getInstance().apply { time = receipt.paymentDate }
                    val hour = cal.get(Calendar.HOUR_OF_DAY)
                    val label = String.format("%02d:00", hour)
                    groupedData[label] = groupedData.getOrDefault(label, 0) + 1
                }
            }
            "Weekly" -> {
                val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                daysOfWeek.forEachIndexed { index, day ->
                    groupedData[day] = 0
                    sortOrder[day] = index.toLong()
                }

                filteredReceipts.forEach { receipt ->
                    val cal = Calendar.getInstance().apply { time = receipt.paymentDate }
                    val dayIndex = cal.get(Calendar.DAY_OF_WEEK) - 1
                    val label = daysOfWeek[dayIndex]
                    groupedData[label] = groupedData.getOrDefault(label, 0) + 1
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

                filteredReceipts.forEach { receipt ->
                    val receiptCal = Calendar.getInstance().apply { time = receipt.paymentDate }
                    val day = receiptCal.get(Calendar.DAY_OF_MONTH)
                    val label = day.toString()
                    groupedData[label] = groupedData.getOrDefault(label, 0) + 1
                }
            }
            "Yearly" -> {
                val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                months.forEachIndexed { index, month ->
                    groupedData[month] = 0
                    sortOrder[month] = index.toLong()
                }

                filteredReceipts.forEach { receipt ->
                    val cal = Calendar.getInstance().apply { time = receipt.paymentDate }
                    val monthIndex = cal.get(Calendar.MONTH)
                    val label = months[monthIndex]
                    groupedData[label] = groupedData.getOrDefault(label, 0) + 1
                }
            }
        }

        val sortedEntries = groupedData.entries.sortedBy { sortOrder[it.key] ?: 0L }
        val trendData = sortedEntries.map { it.value.toFloat() }
        val labels = sortedEntries.map { it.key }

        return Pair(trendData, labels)
    }
}