package com.example.canteen.data

data class Receipt(
    val ReceiptID: String,
    val Payment_Date: String,   // Long
    val Pay_Amount: Double,
    val Payment_Method: String,
)

data class PaymentRecord(
    val id: String,
    val date: String,
    val amount: Double,
    val method: String,
    val refundStatus: String
)