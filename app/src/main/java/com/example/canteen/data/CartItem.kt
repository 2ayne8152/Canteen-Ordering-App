package com.example.canteen.data

import com.example.menumanagement.MenuItemCard

data class  CartItem(
    val menuItem: MenuItem = MenuItem(),
    val quantity: Int = 0
) {
    val totalPrice: Double get() = menuItem.price * quantity
}
