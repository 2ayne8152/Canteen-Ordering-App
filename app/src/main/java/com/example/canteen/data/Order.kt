package com.example.canteen.data

import com.google.firebase.Timestamp

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: String = "PENDING",
    val createdAt: Timestamp = Timestamp.now()
)
