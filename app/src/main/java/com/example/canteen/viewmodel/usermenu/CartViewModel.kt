package com.example.canteen.viewmodel.usermenu

import androidx.lifecycle.ViewModel
import com.example.canteen.data.CartItem
import com.example.canteen.data.MenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CartViewModel : ViewModel() {
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart = _cart.asStateFlow()

    fun addToCart(item: MenuItem, quantity: Int) {
        // merge or append logic here
    }

    fun clearCart() {
        _cart.value = emptyList()
    }
}