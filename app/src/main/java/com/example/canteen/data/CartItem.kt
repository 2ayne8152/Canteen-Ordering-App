package com.example.canteen.data

data class  CartItem(
    val menuItem: MenuItem = MenuItem(),
    val quantity: Int = 0
) {
    val totalPrice: Double get() = menuItem.price * quantity
}
