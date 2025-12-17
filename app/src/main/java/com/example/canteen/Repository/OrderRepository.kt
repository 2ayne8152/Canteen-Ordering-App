package com.example.canteen.repository

import com.example.canteen.data.CartItem
import com.example.canteen.data.Order
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class OrderRepository {

    private val db = FirebaseFirestore.getInstance()
    private val ordersCollection = db.collection("orders")

    suspend fun createOrder(
        userId: String,
        items: List<CartItem>,
        totalAmount: Double
    ): Order {

        val orderId = ordersCollection.document().id

        val order = Order(
            orderId = orderId,
            userId = userId,
            items = items,
            totalAmount = totalAmount,
            status = "PENDING"
        )

        ordersCollection.document(orderId).set(order).await()
        return order
    }

    suspend fun markOrderPaid(orderId: String) {
        ordersCollection
            .document(orderId)
            .update("status", "PAID")
            .await()
    }

    suspend fun getOrder(orderId: String): Order? {
        val snapshot = ordersCollection.document(orderId).get().await()
        return snapshot.toObject(Order::class.java)
    }

    suspend fun getOrdersByUser(userId: String): List<Order> {
        return try {
            val snapshot = ordersCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt")
                .get()
                .await()
            snapshot.toObjects(Order::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

}
