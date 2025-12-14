package com.example.canteen.DAO


import com.example.canteen.data.MenuItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MenuItemDao {

    private val db = FirebaseFirestore.getInstance()

    // 🔹 Collection name MUST match Firebase exactly
    private val menuCollection = db.collection("MenuItem")

    // Get all menu items
    suspend fun getAllMenuItems(): List<MenuItem> {
        return try {
            menuCollection.get().await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject(MenuItem::class.java)
                        ?.copy(id = doc.id)
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Get menu item by ID
    suspend fun getMenuItemById(id: String): MenuItem? {
        return try {
            val doc = menuCollection.document(id).get().await()
            doc.toObject(MenuItem::class.java)
                ?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    // Add or update menu item
    suspend fun saveMenuItem(item: MenuItem) {
        menuCollection.document(item.id).set(item).await()
    }

    // Delete menu item
    suspend fun deleteMenuItem(id: String) {
        menuCollection.document(id).delete().await()
    }
}
