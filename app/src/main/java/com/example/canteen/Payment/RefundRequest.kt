package com.example.canteen.Payment

data class RefundRequest(
    val orderId: String = "",
    val studentId: String = "",
    val refundDetail: String = "",
    val total: Double = 0.0,
    val reason: String = "",
    val requestTime: Long = 0L,
    val refundBy: String = "",
    val remark: String = "",
    val status: String = ""
)

