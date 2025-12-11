package com.example.canteen.repository

import com.example.canteen.data.MenuItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MenuRepository {

    private val db = FirebaseFirestore.getInstance()
    private val menuCollection = db.collection("menu")

    suspend fun getMenuItems(): List<MenuItem> = suspendCoroutine { cont ->
        menuCollection.get()
            .addOnSuccessListener { result ->
                val items = result.documents.mapNotNull { it.toObject(MenuItem::class.java) }
                cont.resume(items)
            }
            .addOnFailureListener { e ->
                cont.resume(emptyList())
            }
    }
}

