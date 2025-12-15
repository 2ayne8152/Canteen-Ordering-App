package com.example.canteen.data

data class Receipt(
    val receiptId: String = "",
    val orderId: String = "",
    val payment_Date: Long = 0L,
    val payment_Method: String = "",
    val pay_Amount: Double = 0.0,
    val refundId: String? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "receiptId" to receiptId,
        "orderId" to orderId,
        "payment_Date" to payment_Date,
        "payment_Method" to payment_Method,
        "pay_Amount" to pay_Amount,
        "refundId" to refundId
    )

    companion object {
        fun fromMap(map: Map<String, Any>): Receipt {
            return Receipt(
                receiptId = map["receiptId"] as String,
                orderId = map["orderId"] as String,
                payment_Date = map["payment_Date"] as Long,
                payment_Method = map["payment_Method"] as String,
                pay_Amount = (map["pay_Amount"] as Number).toDouble(),
                refundId = map["refundId"] as? String
            )
        }
    }
}
