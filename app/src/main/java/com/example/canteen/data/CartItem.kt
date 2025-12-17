package com.example.canteen.data

import com.google.firebase.firestore.Exclude

data class CartItem(
    val menuItem: MenuItem = MenuItem(),
    val quantity: Int = 0
) {
    @get:Exclude
    val totalPrice: Double get() = menuItem.price * quantity
}