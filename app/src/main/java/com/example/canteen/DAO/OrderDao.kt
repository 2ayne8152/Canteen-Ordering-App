package com.example.canteen.data.dao

import com.example.canteen.data.Order
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class OrderDao {

    private val db = FirebaseFirestore.getInstance()
    val ordersCollection = db.collection("orders") // keep public so repository can access document().id

    suspend fun insertOrder(order: Order) {
        ordersCollection.document(order.orderId).set(order).await()
    }

    suspend fun updateOrderStatus(orderId: String, status: String) {
        ordersCollection.document(orderId).update("status", status).await()
    }

    suspend fun getOrderById(orderId: String): Order? {
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
            val orders = snapshot.toObjects(Order::class.java)
            println("Fetching orders for userId: '$userId', found: ${orders.size}")
            orders.forEach { println(it) } // prints each order object
            orders
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
