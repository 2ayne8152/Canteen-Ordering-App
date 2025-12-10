package com.example.canteen.data

data class RefundRequest(
    val refundId: String = "",
    val refundDetail: String = "",
    val reason: String = "",
    val requestTime: Long = 0L,
    val refundBy: String = "",
    val remark: String = "",
    val status: String = ""
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "refundId" to refundId,
        "refundDetail" to refundDetail,
        "reason" to reason,
        "requestTime" to requestTime,
        "refundBy" to refundBy,
        "remark" to remark,
        "status" to status
    )

    companion object {
        fun fromMap(map: Map<String, Any>?): RefundRequest? {
            if (map == null) return null

            return RefundRequest(
                refundId = map["refundId"] as? String ?: "",
                refundDetail = map["refundDetail"] as? String ?: "",
                reason = map["reason"] as? String ?: "",
                requestTime = (map["requestTime"] as? Number)?.toLong() ?: 0L,
                refundBy = map["refundBy"] as? String ?: "",
                remark = map["remark"] as? String ?: "",
                status = map["status"] as? String ?: ""
            )
        }
    }
}



data class RefundItem(
    val refund: RefundRequest,
    val receipt: Receipt
)