package com.example.canteen.repository

import android.util.Log
import com.example.canteen.data.CartItem
import com.example.canteen.data.Order
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.protobuf.LazyStringArrayList.emptyList
import kotlinx.coroutines.tasks.await
import kotlin.collections.emptyList

class OrderRepository {

    private val db = FirebaseFirestore.getInstance()
    private val ordersCollection = db.collection("orders")

    suspend fun createOrder(userId: String, items: List<CartItem>, totalAmount: Double): Order {
        val orderId = ordersCollection.document().id
        val order = Order(
            orderId = orderId,
            userId = userId,
            items = items,
            totalAmount = totalAmount,
            status = "PENDING"
        )

        val batch = db.batch()

        val orderRef = ordersCollection.document(orderId)
        batch.set(orderRef, order)

        val menuCollection = db.collection("MenuItems")
        items.forEach { item ->
            val menuRef = menuCollection.document(item.menuItem.id)
            batch.update(menuRef, "remainQuantity", FieldValue.increment(-item.quantity.toLong()))
        }

        batch.commit().await()

        return order
    }

    suspend fun orderStatusUpdate(orderId: String, status: String) {
        ordersCollection.document(orderId).update("status", status).await()
    }

    suspend fun getOrder(orderId: String): Order? {
        val snapshot = ordersCollection.document(orderId).get().await()
        return snapshot.toObject(Order::class.java)
    }

    fun listenOrdersByUserId(
        userId: String,
        onUpdate: (List<Order>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration {

        return ordersCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                if (snapshot == null) return@addSnapshotListener

                val orders: List<Order> = (snapshot?.documents
                    ?.mapNotNull { it.toObject(Order::class.java) }
                    ?: emptyList()) as List<Order>

                onUpdate(orders)
            }
    }

}
