package com.example.canteen.repository

import com.example.canteen.data.CartItem
import com.example.canteen.data.Order
import com.example.canteen.data.dao.OrderDao
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.protobuf.LazyStringArrayList.emptyList
import kotlinx.coroutines.tasks.await
import kotlin.collections.emptyList

/*class OrderRepository(
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

    fun listenOrdersByUserId(
        userId: String,
        onUpdate: (List<Order>) -> Unit
    ): ListenerRegistration {

        return FirebaseFirestore.getInstance()
            .collection("orders")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val orders = snapshot?.documents?.map {
                    Order.fromMap(it.data!!)
                } ?: emptyList()

                onUpdate(orders)
            }
    }
}*/

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
            status = "Pending"
        )
        ordersCollection.document(orderId).set(order)
        return order
    }

    suspend fun markOrderPaid(orderId: String) {
        ordersCollection.document(orderId).update("status", "Paid")
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

                val orders: List<Order> = (snapshot?.documents
                    ?.mapNotNull { it.toObject(Order::class.java) }
                    ?: emptyList()) as List<Order>

                onUpdate(orders)
            }
    }

}
