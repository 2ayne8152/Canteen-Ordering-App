package com.example.canteen.viewmodel.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

    private val _menuItems = MutableStateFlow<List<FirestoreMenuItem>>(emptyList())
    val menuItems = _menuItems.asStateFlow()

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

    fun updateMenuItem(
        item: FirestoreMenuItem,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                db.collection("menu_items").document(item.id).set(item).await()
                onComplete(true, null)
            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
    }

}


