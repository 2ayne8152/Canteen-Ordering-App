package com.example.canteen.DAO

import com.example.canteen.data.RefundRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class RefundDao {
    private val db = FirebaseFirestore.getInstance()
    private val refundCollection: CollectionReference = db.collection("refund")

    // CREATE
    suspend fun createRefund(refund: RefundRequest): String {
        val docRef = refundCollection.document()   // auto-generate ID
        val refundWithId = refund.copy(refundId = docRef.id)

        docRef.set(refundWithId).await()

        return docRef.id
    }

    // READ ALL
    suspend fun getAllRefunds(): List<RefundRequest> {
        return refundCollection.get().await().toObjects(RefundRequest::class.java)
    }

    // READ by ID
    suspend fun getRefundById(id: String): RefundRequest? {
        val map = refundCollection.document(id).get().await().data
        return RefundRequest.fromMap(map)
    }

    // UPDATE
    suspend fun updateRefund(refundId: String, updates: Map<String, Any>) {
        refundCollection.document(refundId).update(updates).await()
    }

    // DELETE
    suspend fun deleteRefund(refundId: String) {
        refundCollection.document(refundId).delete().await()
    }

    fun listenRefunds(onChange: (List<RefundRequest>) -> Unit) {
        refundCollection.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val list = snapshot.toObjects(RefundRequest::class.java)
                onChange(list)
            }
        }
    }

    fun listenRefund(
        onUpdate: (List<RefundRequest>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return refundCollection
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onError(e)
                    return@addSnapshotListener
                }

                val refunds = snapshot?.documents?.mapNotNull {
                    RefundRequest.fromMap(it.data)
                }.orEmpty()

                onUpdate(refunds)
            }
    }
}