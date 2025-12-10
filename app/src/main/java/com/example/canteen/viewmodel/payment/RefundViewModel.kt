package com.example.canteen.viewmodel.payment

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.canteen.data.RefundRequest

class RefundViewModel : ViewModel() {

    //private val db = FirebaseFirestore.getInstance()

    var pendingList by mutableStateOf<List<RefundRequest>>(emptyList())
        private set

    var approvedList by mutableStateOf<List<RefundRequest>>(emptyList())
        private set

    var rejectedList by mutableStateOf<List<RefundRequest>>(emptyList())
        private set

    init {
        //loadRefundRequests()
    }

    /*private fun loadRefundRequests() {
        db.collection("RefundRequests")
            .addSnapshotListener { snapshot, _ ->

                if (snapshot != null) {
                    val all = snapshot.toObjects(RefundRequest::class.java)

                    pendingList = all.filter { it.status == "pending" }
                    approvedList = all.filter { it.status == "approved" }
                    rejectedList = all.filter { it.status == "rejected" }
                }
            }
    }*/
}
