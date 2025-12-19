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

    // Error messages for UI to display
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun addToCart(item: MenuItem, quantity: Int): Boolean {
        if (quantity <= 0) return false

        val current = _cart.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.menuItem.id == item.id }

        // Calculate what the new total quantity would be
        val currentQuantityInCart = if (existingIndex >= 0) {
            current[existingIndex].quantity
        } else {
            0
        }
        val newTotalQuantity = currentQuantityInCart + quantity

        // Check against available stock (remainQuantity)
        val availableStock = item.remainQuantity // or item.remainQuantity depending on your model

        if (newTotalQuantity > availableStock) {
            // Stock limit exceeded!
            _errorMessage.value = if (currentQuantityInCart > 0) {
                "Cannot add $quantity more. You already have $currentQuantityInCart in cart. Only $availableStock available!"
            } else {
                "Cannot add $quantity items. Only $availableStock available!"
            }
            return false
        }

        // Stock check passed - add to cart
        if (existingIndex >= 0) {
            val existing = current[existingIndex]
            current[existingIndex] = existing.copy(quantity = newTotalQuantity)
        } else {
            current.add(CartItem(item, quantity))
        }

        _cart.value = current
        _errorMessage.value = null // Clear any previous errors
        return true
    }

    /**
     * Set the quantity for an item. If qty <= 0, the item will be removed.
     * Returns false if the update would exceed available stock.
     */
    fun updateQuantity(itemId: String, qty: Int): Boolean {
        val current = _cart.value.toMutableList()
        val idx = current.indexOfFirst { it.menuItem.id == itemId }
        if (idx < 0) return false

        if (qty <= 0) {
            // Remove item
            current.removeAt(idx)
            _cart.value = current
            _errorMessage.value = null
            return true
        }

        // Check against available stock
        val item = current[idx].menuItem
        val availableStock = item.remainQuantity // or item.remainQuantity

        if (qty > availableStock) {
            _errorMessage.value = "Cannot set quantity to $qty. Only $availableStock available!"
            return false
        }

        // Update quantity
        current[idx] = current[idx].copy(quantity = qty)
        _cart.value = current
        _errorMessage.value = null
        return true
    }

    fun removeItem(itemId: String) {
        _cart.value = _cart.value.filterNot { it.menuItem.id == itemId }
        _errorMessage.value = null
    }

    fun clearCart() {
        _cart.value = emptyList()
        _errorMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // Flows for totals
    val totalItems = cart.map { list -> list.sumOf { it.quantity } }
    val totalPrice = cart.map { list -> list.sumOf { it.totalPrice } }
}