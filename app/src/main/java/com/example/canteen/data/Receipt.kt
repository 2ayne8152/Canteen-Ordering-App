package com.example.canteen.data

import com.example.canteen.data.RefundRequest

data class Receipt(
    val receiptId: String = "",
    val orderId: String = "",
    val payment_Date: Long = 0L,
    val payment_Method: String = "",
    val pay_Amount: Double = 0.0,
    val refund : RefundRequest? = null
)