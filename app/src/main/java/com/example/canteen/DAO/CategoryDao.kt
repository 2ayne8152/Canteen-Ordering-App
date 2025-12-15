package com.example.canteen.DAO


import com.example.canteen.viewmodel.login.Category
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CategoryDao {

    private val db = FirebaseFirestore.getInstance()
    private val categoryCollection = db.collection("Category") // Use your collection name

    suspend fun getAllCategories(): List<Category> {
        return try {
            categoryCollection.get().await().documents.mapNotNull {
                it.toObject(Category::class.java)?.copy(CategoryID = it.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
