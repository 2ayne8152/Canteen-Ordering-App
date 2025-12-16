package com.example.canteen.viewmodel.usermenu

import androidx.lifecycle.ViewModel
import com.example.canteen.data.CartItem
import com.example.canteen.data.MenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class CartViewModel : ViewModel() {

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    fun addToCart(item: MenuItem, quantity: Int) {
        if (quantity <= 0) return

        val current = _cart.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.menuItem.id == item.id }

        if (existingIndex >= 0) {
            val existing = current[existingIndex]
            current[existingIndex] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            current.add(CartItem(item, quantity))
        }
        _cart.value = current
    }

    /**
     * Set the quantity for an item. If qty <= 0, the item will be removed.
     */
    fun updateQuantity(itemId: String, qty: Int) {
        val current = _cart.value.toMutableList()
        val idx = current.indexOfFirst { it.menuItem.id == itemId }
        if (idx < 0) return
        if (qty <= 0) {
            current.removeAt(idx)
        } else {
            current[idx] = current[idx].copy(quantity = qty)
        }
        _cart.value = current
    }

    fun removeItem(itemId: String) {
        _cart.value = _cart.value.filterNot { it.menuItem.id == itemId }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    // Flows for totals
    val totalItems = cart.map { list -> list.sumOf { it.quantity } }
    val totalPrice = cart.map { list -> list.sumOf { it.totalPrice } }
}
