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

// Full FirestoreMenuItem including staff-editable fields
data class FirestoreMenuItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val categoryId: String = "",
    val remainQuantity: Int = 0,
    val imageUrl: String = ""
)

class MenuViewModel : ViewModel() {

    private val db = Firebase.firestore

    // Menu items
    private val _menuItems = MutableStateFlow<List<FirestoreMenuItem>>(emptyList())
    val menuItems = _menuItems.asStateFlow()

    // Cart related
    private val _cart = MutableStateFlow<Map<FirestoreMenuItem, Int>>(emptyMap())
    val cart = _cart.asStateFlow()
    private val _numOfItem = MutableStateFlow(0)
    val numOfItem = _numOfItem.asStateFlow()
    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice = _totalPrice.asStateFlow()

    init {
        fetchMenuItems()
    }

    // Fetch menu items from Firestore
    private fun fetchMenuItems() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("menu_items").get().await()
                // include document ID for updates
                _menuItems.value = snapshot.documents.map { doc ->
                    doc.toObject(FirestoreMenuItem::class.java)!!.copy(id = doc.id)
                }
            } catch (e: Exception) {
                Log.e("MenuViewModel", "Error fetching menu items", e)
            }
        }
    }

    // Update menu item (staff edit)
    fun updateMenuItem(item: FirestoreMenuItem, onComplete: (Boolean, String?) -> Unit) {
        if (item.id.isBlank()) {
            onComplete(false, "Document ID is empty")
            return
        }
        viewModelScope.launch {
            try {
                db.collection("menu_items").document(item.id).set(item).await()
                fetchMenuItems() // refresh after update
                onComplete(true, null)
            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
    }

    // Cart operations
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
