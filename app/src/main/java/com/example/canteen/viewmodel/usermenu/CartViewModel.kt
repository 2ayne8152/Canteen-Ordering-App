package com.example.canteen.viewmodel.usermenu

import androidx.lifecycle.ViewModel
import com.example.canteen.data.CartItem
import com.example.canteen.data.MenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class CartViewModel : ViewModel() {

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart = _cart.asStateFlow()

    fun addToCart(item: MenuItem, quantity: Int) {
        val current = _cart.value.toMutableList()

        val existingIndex = current.indexOfFirst { it.menuItem.id == item.id }

        if (existingIndex >= 0) {
            val updated = current[existingIndex].copy(
                quantity = current[existingIndex].quantity + quantity
            )
            current[existingIndex] = updated
        } else {
            current.add(CartItem(item, quantity))
        }

        _cart.value = current
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    val totalItems = cart.map { list ->
        list.sumOf { it.quantity }
    }

    val totalPrice = cart.map { list ->
        list.sumOf { it.totalPrice }
    }
}

