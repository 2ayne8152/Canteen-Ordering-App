package com.example.canteen.DAO

import com.example.canteen.data.Receipt
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class ReceiptDao {

    private val receiptCollection = FirebaseFirestore.getInstance()
        .collection("receipt")

    suspend fun createReceipt(receipt: Receipt) {
        receiptCollection.document(receipt.receiptId).set(receipt.toMap()).await()
    }

    suspend fun getReceiptById(id: String): Map<String, Any>? {
        return receiptCollection.document(id).get().await().data
    }

    suspend fun updateReceipt(id: String, data: Map<String, Any?>) {
        receiptCollection.document(id).update(data).await()
    }

    suspend fun deleteReceipt(id: String) {
        receiptCollection.document(id).delete().await()
    }

    fun listenReceiptById(
        id: String,
        onUpdate: (Receipt?) -> Unit
    ): ListenerRegistration {
        return receiptCollection.document(id).addSnapshotListener { snap, _ ->
            onUpdate(snap?.data?.let { Receipt.fromMap(it) })
        }
    }

    fun listenReceiptList(
        onUpdate: (List<Receipt>) -> Unit
    ): ListenerRegistration {
        return receiptCollection.addSnapshotListener { snap, _ ->
            val list = snap?.documents?.mapNotNull {
                val data = it.data ?: return@mapNotNull null
                Receipt.fromMap(data)
            } ?: emptyList()

            onUpdate(list)
        }
    }
}
