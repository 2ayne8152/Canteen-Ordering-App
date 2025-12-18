package com.example.canteen.DAO

import com.example.canteen.data.MenuItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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

    fun listenMenuItems(
        onUpdate: (List<MenuItem>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration {
        return menuCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }

            val items = snapshot?.documents?.mapNotNull {
                it.toObject(MenuItem::class.java)?.copy(id = it.id)
            } ?: emptyList()

            onUpdate(items)
        }
    }

    suspend fun saveMenuItem(item: MenuItem) {
        menuCollection.document(item.id).set(item).await()
    }

    suspend fun deleteMenuItem(id: String) {
        menuCollection.document(id).delete().await()
    }
}
