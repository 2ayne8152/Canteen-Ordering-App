package com.example.canteen.DAO

import com.example.canteen.data.MenuItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MenuItemDao {

    private val db = FirebaseFirestore.getInstance()
    private val menuCollection = db.collection("MenuItems")

    suspend fun getAllMenuItems(): List<MenuItem> {
        return try {
            menuCollection.get().await().documents.mapNotNull {
                it.toObject(MenuItem::class.java)?.copy(id = it.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveMenuItem(item: MenuItem) {
        menuCollection.document(item.id).set(item).await()
    }

    suspend fun deleteMenuItem(id: String) {
        menuCollection.document(id).delete().await()
    }
}
