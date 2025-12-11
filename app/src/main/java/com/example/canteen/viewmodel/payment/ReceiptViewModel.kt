package com.example.canteen.viewmodel.payment

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.canteen.Repository.ReceiptRepository
import com.example.canteen.data.Receipt
import com.example.canteen.data.RefundRequest
import androidx.compose.runtime.State
import androidx.lifecycle.viewModelScope
import com.example.canteen.Repository.RefundRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReceiptViewModel(
    private val repository: ReceiptRepository = ReceiptRepository()
) : ViewModel() {

    private val _receipt = mutableStateOf<Receipt?>(null)
    val receipt: State<Receipt?> = _receipt

    private val _refund = mutableStateOf<RefundRequest?>(null)
    val refund: State<RefundRequest?> = _refund

    private val _receiptList = MutableStateFlow<List<Pair<Receipt, RefundRequest?>>>(emptyList())
    val receiptList = _receiptList.asStateFlow()

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _newReceiptId = MutableStateFlow<String?>(null)
    val newReceiptId: StateFlow<String?> = _newReceiptId

    private var receiptListener: ListenerRegistration? = null

    fun loadReceipt(id: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val pair = repository.getReceiptWithRefund(id)
                if (pair != null) {
                    _receipt.value = pair.first
                    _refund.value = pair.second
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun createReceipt(
        orderId: String,
        paymentMethod: String,
        amount: Double
    ) {
        viewModelScope.launch {
            _newReceiptId.value = null
            _loading.value = true
            try {
                val receipt =  repository.createReceipt(orderId, paymentMethod, amount)
                _newReceiptId.value = receipt.receiptId

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun listenReceipt(id: String) {
        receiptListener?.remove() // clear any existing listener

        receiptListener = repository.listenReceipt(id) { receipt, refund ->
            _receipt.value = receipt
            _refund.value = refund
        }
    }


    // ---------------------------------------------------------
    // LOAD ALL receipts + refund
    // ---------------------------------------------------------
    fun loadAllReceipts() {
        viewModelScope.launch {
            _loading.value = true
            try {
                _receiptList.value = repository.loadReceiptList()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }


    // ---------------------------------------------------------
    // UPDATE Receipt
    // ---------------------------------------------------------
    fun updateReceipt(id: String, updates: Map<String, Any?>) {
        viewModelScope.launch {
            try {
                repository.updateReceipt(id, updates)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }


    // ---------------------------------------------------------
    // DELETE Receipt
    // ---------------------------------------------------------
    fun deleteReceipt(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteReceipt(id)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }


    // ---------------------------------------------------------
    // Cleanup listener when ViewModel destroyed
    // ---------------------------------------------------------
    override fun onCleared() {
        super.onCleared()
        receiptListener?.remove()
    }
}

/*
@Composable
fun ReceiptDetailScreen(receiptId: String, vm: ReceiptViewModel = viewModel()) {

    LaunchedEffect(receiptId) {
        vm.loadReceipt(receiptId)
    }

    val receipt = vm.receipt.value
    val refund = vm.refund.value
    val loading = vm.loading.value

    if (loading) {
        Text("Loading…")
        return
    }

    receipt?.let {
        Text("Receipt ID: ${it.receiptId}")
        Text("Amount: RM${it.pay_Amount}")

        if (refund != null) {
            Text("Refund Status: ${refund.status}")
            Text("Reason: ${refund.reason}")
        } else {
            Text("No refund requested")
        }
    }
}

 */