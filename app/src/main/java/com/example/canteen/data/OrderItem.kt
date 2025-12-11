package com.example.canteen.data

data class Order(
    val id: String = "", // generate on server or client
    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val createdAtMillis: Long = System.currentTimeMillis()
)
