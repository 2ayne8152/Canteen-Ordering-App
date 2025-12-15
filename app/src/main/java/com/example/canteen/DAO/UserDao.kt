package com.example.canteen.DAO

import com.example.canteen.data.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserDao {

    private val db = FirebaseFirestore.getInstance()
    private val usersRef = db.collection("users")

    suspend fun getAllUsers(): List<User> {
        return usersRef
            .get()
            .await()
            .toObjects(User::class.java)
    }

    suspend fun getUserById(userId: String): User? {
        val snapshot = usersRef.document(userId).get().await()
        return snapshot.toObject(User::class.java)
    }
}
