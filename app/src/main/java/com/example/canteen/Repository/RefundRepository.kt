package com.example.canteen.Repository

import com.example.canteen.DAO.RefundDao
import com.example.canteen.data.RefundRequest

class RefundRepository(
    private val dao: RefundDao = RefundDao()
) {

    // CREATE
    suspend fun createRefund(refund: RefundRequest): String {
        return dao.createRefund(refund)
    }

    // READ ALL
    suspend fun getAllRefunds(): List<RefundRequest> {
        return dao.getAllRefunds()
    }


    // READ BY ID
    suspend fun getRefundById(id: String): RefundRequest? {
        return dao.getRefundById(id)
    }



    // UPDATE
    suspend fun updateRefund(id: String, updates: Map<String, Any>) {
        dao.updateRefund(id, updates)
    }

    // DELETE
    suspend fun deleteRefund(id: String) {
        dao.deleteRefund(id)
    }

    fun listenRefunds(onChange: (List<RefundRequest>) -> Unit) {
        dao.listenRefunds(onChange)
    }

}