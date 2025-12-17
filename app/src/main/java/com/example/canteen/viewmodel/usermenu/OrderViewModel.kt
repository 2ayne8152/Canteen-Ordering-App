package com.example.canteen.viewmodel.usermenu

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canteen.data.CartItem
import com.example.canteen.data.Order
import com.example.canteen.repository.OrderRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrderViewModel(
    private val repository: OrderRepository = OrderRepository()
) : ViewModel() {
    private val _latestOrder = MutableStateFlow<Order?>(null)
    val latestOrder: StateFlow<Order?> = _latestOrder

    private val _refundOrder = MutableStateFlow<Order?>(null)
    val refundOrder: StateFlow<Order?> = _refundOrder

    private val _orders = MutableStateFlow<Map<String, Order>>(emptyMap())
    val orders: StateFlow<Map<String, Order>> = _orders

    suspend fun createOrder(
        userId: String,
        items: List<CartItem>,
        totalAmount: Double
    ): Order {
        val order = repository.createOrder(userId, items, totalAmount)
        _latestOrder.value = order
        return order
    }


    fun orderStatusUpdate(orderId: String, status: String) {
        viewModelScope.launch {
            repository.orderStatusUpdate(orderId, status)
        }
    }

    fun getOrder(orderId: String) {
        viewModelScope.launch {
            _refundOrder.value = repository.getOrder(orderId)
        }
    }

    fun getOrderForHistory(orderId: String) {
        if (orderId.isBlank()) return

        viewModelScope.launch {
            if (_orders.value.containsKey(orderId)) return@launch

            repository.getOrder(orderId)?.let { order ->
                _orders.update { it + (orderId to order) }
            }
        }
    }

    private val _orderHistory = MutableStateFlow<List<Order>>(emptyList())
    val orderHistory: StateFlow<List<Order>> = _orderHistory
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    private var orderListener: ListenerRegistration? = null
    private val _selectedOrder = MutableStateFlow<Order?>(null)
    val selectedOrder: StateFlow<Order?> = _selectedOrder

    fun startListeningOrderHistory(userId: String) {
        stopListeningOrderHistory()

        orderListener = repository.listenOrdersByUserId(
            userId = userId,
            onUpdate = { orders ->
                _orderHistory.value = orders
            },
            onError = { throwable ->
                _error.value = throwable.message
            }
        )
    }

    fun stopListeningOrderHistory() {
        orderListener?.remove()
        orderListener = null
    }

    override fun onCleared() {
        stopListeningOrderHistory()
        super.onCleared()
    }

    fun selectOrder(order: Order) {
        _selectedOrder.value = order
    }
}
