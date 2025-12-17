package com.example.canteen.repository

import com.example.canteen.DAO.ReceiptDao
import com.example.canteen.DAO.RefundDao
import com.example.canteen.data.Receipt
import com.example.canteen.data.RefundRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class ReceiptRepository(
    private val receiptDao: ReceiptDao = ReceiptDao(),
    private val refundDao: RefundDao = RefundDao()// your existing DAO
) {

    //READ (Receipt + Refund)
    suspend fun getReceiptWithRefund(id: String): Pair<Receipt, RefundRequest?>? {
        val receiptData = receiptDao.getReceiptById(id) ?: return null
        val receipt = Receipt.fromMap(receiptData)
        val refund = receipt.refundId?.let { refundDao.getRefundById(it) }
        return Pair(receipt, refund)
    }

    suspend fun getReceiptWithRefundByOrderId(orderId: String)
            : Pair<Receipt, RefundRequest?>? {

        val receiptData = receiptDao.getReceiptByOrderId(orderId) ?: return null
        val receipt = Receipt.fromMap(receiptData)
        val refund = receipt.refundId?.let { refundDao.getRefundById(it) }

        return Pair(receipt, refund)
    }

    suspend fun createReceipt(
        orderId: String,
        paymentMethod: String,
        paymentAmount: Double
    ): Receipt {

        val receiptId = UUID.randomUUID().toString()

        val receipt = Receipt(
            receiptId = receiptId,
            orderId = orderId,
            payment_Date = System.currentTimeMillis(),
            payment_Method = paymentMethod,
            pay_Amount = paymentAmount
        )

        receiptDao.createReceipt(receipt)
        return receipt
    }

    // UPDATE
    suspend fun updateReceipt(id: String, data: Map<String, Any?>) {
        receiptDao.updateReceipt(id, data)
    }

    // DELETE
    suspend fun deleteReceipt(id: String) {
        receiptDao.deleteReceipt(id)
    }


    // LISTEN (Live Update for One Receipt)
    fun listenReceipt(
        id: String,
        onChange: (Receipt?, RefundRequest?) -> Unit
    ): ListenerRegistration {
        return receiptDao.listenReceiptById(id) { receipt ->
            if (receipt == null) {
                onChange(null, null)
                return@listenReceiptById
            }

            CoroutineScope(Dispatchers.IO).launch {
                val refund = receipt.refundId?.let { refundDao.getRefundById(it) }
                withContext(Dispatchers.Main) {
                    onChange(receipt, refund)
                }
            }
        }
    }


    // LOAD LIST (All receipts + refund)
    fun loadAllReceipts(): List<Pair<Receipt, RefundRequest?>> {
        val rawList = receiptDao
            .listenReceiptList {} // we only want the once-off, so ignore listener
        return emptyList()
    }

    suspend fun loadReceiptList(): List<Pair<Receipt, RefundRequest?>> {
        val snapshot = FirebaseFirestore.getInstance()
            .collection("receipt")
            .get()
            .await()

        val result = mutableListOf<Pair<Receipt, RefundRequest?>>()

        for (doc in snapshot.documents) {
            val data = doc.data ?: continue
            val receipt = Receipt.fromMap(data)
            val refund = receipt.refundId?.let { refundDao.getRefundById(it) }
            result.add(Pair(receipt, refund))
        }

        return result
    }

    private var receiptListener: ListenerRegistration? = null
    private var refundListener: ListenerRegistration? = null

    fun listenReceiptWithRefund(
        onUpdate: (List<Pair<Receipt, RefundRequest?>>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        var receipts: List<Receipt> = emptyList()
        var refunds: List<RefundRequest> = emptyList()

        fun emit() {
            val refundMap = refunds.associateBy { it.refundId }

            val combined = receipts.map { receipt ->
                receipt to refundMap[receipt.refundId]
            }

            onUpdate(combined)
        }

        receiptListener = receiptDao.listenReceipts(
            onUpdate = {
                receipts = it
                emit()
            },
            onError = onError
        )

        refundListener = refundDao.listenRefund(
            onUpdate = {
                refunds = it
                emit()
            },
            onError = onError
        )
    }

    fun removeListeners() {
        receiptListener?.remove()
        refundListener?.remove()
    }

}
