package com.example.canteen.repository

import com.example.canteen.data.CartItem
import com.example.canteen.data.Order
import com.example.canteen.data.dao.OrderDao

class OrderRepository(
    private val dao: OrderDao = OrderDao()
) {

    suspend fun createOrder(
        userId: String,
        items: List<CartItem>,
        totalAmount: Double
    ): Order {
        val orderId = dao.ordersCollection.document().id

        val order = Order(
            orderId = orderId,
            userId = userId,
            items = items,
            totalAmount = totalAmount,
            status = "PENDING"
        )

        dao.insertOrder(order)
        return order
    }

    suspend fun markOrderPaid(orderId: String) {
        dao.updateOrderStatus(orderId, "PAID")
    }

    suspend fun getOrder(orderId: String): Order? {
        return dao.getOrderById(orderId)
    }

    suspend fun getOrdersByUser(userId: String): List<Order> {
        return dao.getOrdersByUser(userId)
    }
}
