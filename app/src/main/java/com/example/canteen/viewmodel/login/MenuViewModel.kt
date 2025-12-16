package com.example.canteen.viewmodel.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ---------------- DATA ----------------

data class FirestoreMenuItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val categoryId: String = "",
    val remainQuantity: Int = 0,
    val imageUrl: String = ""
)

data class Category(
    val categoryId: String = "",
    val name: String = "",
    val description: String = ""
)

// ---------------- VIEWMODEL ----------------

class MenuViewModel : ViewModel() {

    private val db = Firebase.firestore

    // Menu
    private val _menuItems = MutableStateFlow<List<FirestoreMenuItem>>(emptyList())
    val menuItems = _menuItems.asStateFlow()

    // Categories
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    // Cart (key = menuItemId)
    private val _cart =
        MutableStateFlow<Map<String, Pair<FirestoreMenuItem, Int>>>(emptyMap())
    val cart = _cart.asStateFlow()

    private val _numOfItem = MutableStateFlow(0)
    val numOfItem = _numOfItem.asStateFlow()

    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice = _totalPrice.asStateFlow()

    init {
        fetchCategories()
        fetchMenuItems()
    }

    // ---------------- FETCH ----------------

    private fun fetchCategories() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("Category").get().await()
                _categories.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Category::class.java)?.copy(categoryId = doc.id)
                }
            } catch (e: Exception) {
                Log.e("MenuViewModel", "Fetch categories failed", e)
            }
        }
    }

    private fun fetchMenuItems() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("MenuItems").get().await()
                _menuItems.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FirestoreMenuItem::class.java)?.copy(id = doc.id)
                }
            } catch (e: Exception) {
                Log.e("MenuViewModel", "Fetch menu items failed", e)
            }
        }
    }

    // ---------------- CREATE / UPDATE ----------------

    fun createMenuItem(
        menuItem: FirestoreMenuItem,
        categoryName: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val categorySnapshot = db.collection("Category")
                    .whereEqualTo("name", categoryName)
                    .get()
                    .await()

                val categoryId = if (categorySnapshot.isEmpty) {
                    val ref = db.collection("Category").document()
                    ref.set(
                        Category(
                            categoryId = ref.id,
                            name = categoryName,
                            description = ""
                        )
                    ).await()
                    ref.id
                } else {
                    categorySnapshot.documents[0].id
                }

                val menuRef = db.collection("MenuItems").document()
                menuRef.set(menuItem.copy(categoryId = categoryId)).await()

                fetchMenuItems()
                onComplete(true, null)
            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
    }

    fun updateMenuItem(
        item: FirestoreMenuItem,
        onComplete: (Boolean, String?) -> Unit
    ) {
        if (item.id.isBlank()) {
            onComplete(false, "Menu item id is empty")
            return
        }

        viewModelScope.launch {
            try {
                db.collection("MenuItems").document(item.id).set(item).await()
                fetchMenuItems()
                onComplete(true, null)
            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
    }

    // ---------------- CART ----------------

    fun addToCart(item: FirestoreMenuItem) {
        _cart.update {
            val map = it.toMutableMap()
            val current = map[item.id]
            map[item.id] = Pair(item, (current?.second ?: 0) + 1)
            map
        }
        updateCartTotals()
    }

    fun removeFromCart(item: FirestoreMenuItem) {
        _cart.update {
            val map = it.toMutableMap()
            val current = map[item.id]
            if (current != null) {
                if (current.second > 1)
                    map[item.id] = Pair(item, current.second - 1)
                else
                    map.remove(item.id)
            }
            map
        }
        updateCartTotals()
    }

    private fun updateCartTotals() {
        _numOfItem.value = _cart.value.values.sumOf { it.second }
        _totalPrice.value =
            _cart.value.values.sumOf { it.first.price * it.second }
    }
}
