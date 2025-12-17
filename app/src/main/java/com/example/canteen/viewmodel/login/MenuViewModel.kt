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

data class FirestoreMenuItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val categoryId: String = "",
    val remainQuantity: Int = 0,
    val imageUrl: String = "" // Base64 string
)

data class Category(
    val CategoryID: String = "",
    val Name: String = "",
    val Description: String = ""
)

class MenuViewModel : ViewModel() {

    private val db = Firebase.firestore

    // ---------------- MENU ----------------
    private val _menuItems = MutableStateFlow<List<FirestoreMenuItem>>(emptyList())
    val menuItems = _menuItems.asStateFlow()

    // ---------------- CATEGORIES ----------------
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    // ---------------- CART ----------------
    private val _cart = MutableStateFlow<Map<FirestoreMenuItem, Int>>(emptyMap())
    val cart = _cart.asStateFlow()

    private val _numOfItem = MutableStateFlow(0)
    val numOfItem = _numOfItem.asStateFlow()

    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice = _totalPrice.asStateFlow()

    init {
        fetchCategories()
        fetchMenuItems()
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("Category").get().await()
                _categories.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Category::class.java)?.copy(CategoryID = doc.id)
                }
            } catch (e: Exception) {
                Log.e("MenuViewModel", "Fetch categories error", e)
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
                Log.e("MenuViewModel", "Fetch menu items error", e)
            }
        }
    }

    // Create menu item + optionally category
    fun createMenuItem(
        menuItem: FirestoreMenuItem,
        categoryName: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                var categoryId = menuItem.categoryId

                // Check if category exists
                val categorySnapshot = db.collection("Category")
                    .whereEqualTo("Name", categoryName)
                    .get()
                    .await()

                categoryId = if (categorySnapshot.documents.isEmpty()) {
                    // Category does not exist, create it
                    val newCategoryRef = db.collection("Category").document()
                    val newCategory = hashMapOf(
                        "CategoryID" to newCategoryRef.id,
                        "Name" to categoryName,
                        "Description" to ""
                    )
                    newCategoryRef.set(newCategory).await()
                    newCategoryRef.id
                } else {
                    categorySnapshot.documents[0].getString("CategoryID") ?: ""
                }

                // Create MenuItem with categoryId
                val menuRef = db.collection("MenuItems").document()
                val newMenuItem = menuItem.copy(categoryId = categoryId)
                menuRef.set(newMenuItem).await()

                fetchMenuItems()
                fetchCategories()
                onComplete(true, null)
            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
    }
    fun deleteMenuItem(
        itemId: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        if (itemId.isBlank()) {
            onComplete(false, "Item ID is empty")
            return
        }

        viewModelScope.launch {
            try {
                Firebase.firestore
                    .collection("MenuItems")
                    .document(itemId)
                    .delete()
                    .await()

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
            onComplete(false, "Document ID empty")
            return
        }

        viewModelScope.launch {
            try {
                db.collection("MenuItems")
                    .document(item.id)
                    .set(item)
                    .await()

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
            map[item] = (map[item] ?: 0) + 1
            map
        }
        updateCartTotals()
    }

    fun removeFromCart(item: FirestoreMenuItem) {
        _cart.update {
            val map = it.toMutableMap()
            val qty = map[item] ?: 0
            if (qty > 1) map[item] = qty - 1 else map.remove(item)
            map
        }
        updateCartTotals()
    }

    private fun updateCartTotals() {
        _numOfItem.value = _cart.value.values.sum()
        _totalPrice.value = _cart.value.entries.sumOf { it.key.price * it.value }
    }
}
