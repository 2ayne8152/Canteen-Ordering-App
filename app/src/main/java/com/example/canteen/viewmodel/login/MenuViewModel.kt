package com.example.canteen.viewmodel.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Data class for Firestore documents
data class FirestoreMenuItem(
    val id: String = "",
    val categoryId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val remainQuantity: Int = 0,
    val imageUrl: String = ""
)


class MenuViewModel : ViewModel() {

    private val db = Firebase.firestore

    // Holds the list of all menu items fetched from Firestore
    private val _menuItems = MutableStateFlow<List<FirestoreMenuItem>>(emptyList())
    val menuItems = _menuItems.asStateFlow()

    // Holds the items in the shopping cart (MenuItem to quantity)
    private val _cart = MutableStateFlow<Map<FirestoreMenuItem, Int>>(emptyMap())
    val cart = _cart.asStateFlow()

    // Holds the total number of items in the cart
    private val _numOfItem = MutableStateFlow(0)
    val numOfItem = _numOfItem.asStateFlow()

    // Holds the total price of items in the cart
    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice = _totalPrice.asStateFlow()

    init {
        fetchMenuItems()
    }

    private fun fetchMenuItems() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("menu_items").get().await()
                _menuItems.value = snapshot.toObjects<FirestoreMenuItem>()
            } catch (e: Exception) {
                Log.e("MenuViewModel", "Error fetching menu items", e)
            }
        }
    }

    fun addToCart(item: FirestoreMenuItem) {
        _cart.update { currentCart ->
            val newCart = currentCart.toMutableMap()
            newCart[item] = (newCart[item] ?: 0) + 1
            newCart
        }
        updateCartTotals()
    }

    fun removeFromCart(item: FirestoreMenuItem) {
        _cart.update { currentCart ->
            val newCart = currentCart.toMutableMap()
            val currentQuantity = newCart[item] ?: 0
            if (currentQuantity > 1) {
                newCart[item] = currentQuantity - 1
            } else {
                newCart.remove(item)
            }
            newCart
        }
        updateCartTotals()
    }

    private fun updateCartTotals() {
        _numOfItem.value = _cart.value.values.sum()
        _totalPrice.value = _cart.value.map { (item, quantity) -> item.price * quantity }.sum()
    }
}
