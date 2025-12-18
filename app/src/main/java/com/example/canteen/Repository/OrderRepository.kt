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
    private val menuCollection = db.collection("MenuItems")

    suspend fun createOrder(userId: String, items: List<CartItem>, totalAmount: Double): Order {
        // Use Firestore transaction to ensure atomic stock check and update
        return db.runTransaction { transaction ->
            val orderId = ordersCollection.document().id

            // Step 1: Validate stock for ALL items first
            val stockValidation = mutableListOf<Pair<String, Long>>() // itemName, availableStock

            for (cartItem in items) {
                val menuRef = menuCollection.document(cartItem.menuItem.id)
                val menuSnapshot = transaction.get(menuRef)

                if (!menuSnapshot.exists()) {
                    throw Exception("Item '${cartItem.menuItem.name}' no longer exists!")
                }

                val currentStock = menuSnapshot.getLong("remainQuantity") ?: 0L

                if (currentStock < cartItem.quantity) {
                    // Not enough stock!
                    throw Exception(
                        "Insufficient stock for '${cartItem.menuItem.name}'! " +
                                "Only $currentStock available, but you tried to order ${cartItem.quantity}."
                    )
                }

                stockValidation.add(cartItem.menuItem.name to currentStock)
            }

            // Step 2: All validations passed - now deduct stock and create order
            val order = Order(
                orderId = orderId,
                userId = userId,
                items = items,
                totalAmount = totalAmount,
                status = "PENDING"
            )

            // Deduct stock for each item
            items.forEach { cartItem ->
                val menuRef = menuCollection.document(cartItem.menuItem.id)
                transaction.update(
                    menuRef,
                    "remainQuantity",
                    FieldValue.increment(-cartItem.quantity.toLong())
                )
            }

            // Create the order
            val orderRef = ordersCollection.document(orderId)
            transaction.set(orderRef, order)

            Log.d("OrderRepository", "Order created successfully: $orderId")
            order

        }.await()
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