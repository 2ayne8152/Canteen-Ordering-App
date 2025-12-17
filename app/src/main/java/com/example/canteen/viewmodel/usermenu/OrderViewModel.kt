package com.example.canteen.viewmodel.usermenu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canteen.data.CartItem
import com.example.canteen.data.Order
import com.example.canteen.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrderViewModel(
    private val repository: OrderRepository = OrderRepository()
) : ViewModel() {

    private val _latestOrder = MutableStateFlow<Order?>(null)
    val latestOrder: StateFlow<Order?> = _latestOrder

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    fun createOrder(
        userId: String,
        items: List<CartItem>,
        totalAmount: Double
    ) {
        viewModelScope.launch {
            val order = repository.createOrder(userId, items, totalAmount)
            _latestOrder.value = order
        }
    }

    fun markOrderPaid(orderId: String) {
        viewModelScope.launch {
            repository.markOrderPaid(orderId)
        }
    }

    fun getOrder(orderId: String) {
        viewModelScope.launch {
            _latestOrder.value = repository.getOrder(orderId)
        }
    }

    fun loadOrdersByUser(userId: String) {
        val trimmedUserId = userId.trim()
        viewModelScope.launch {
            try {
                val fetchedOrders = repository.getOrdersByUser(trimmedUserId)
                _orders.value = fetchedOrders
            } catch (e: Exception) {
                _orders.value = emptyList() // avoid crash if Firestore fails
            }
        }
    }
}
