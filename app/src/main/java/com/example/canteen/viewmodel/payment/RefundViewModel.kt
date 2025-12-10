package com.example.canteen.viewmodel.payment

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.canteen.data.RefundRequest
import androidx.lifecycle.viewModelScope
import com.example.canteen.DAO.RefundDao
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.canteen.Repository.RefundRepository
import androidx.compose.runtime.State

class RefundViewModel(
    private val repository: RefundRepository = RefundRepository()
) : ViewModel() {
    private val _refundList = MutableStateFlow<List<RefundRequest>>(emptyList())
    val refundList: StateFlow<List<RefundRequest>> = _refundList

    private val _refund = mutableStateOf<RefundRequest?>(null)
    val refund: State<RefundRequest?> = _refund

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // CREATE
    fun createRefund(reason: String, detail: String) {
        viewModelScope.launch {
            try {
                val refund = RefundRequest(
                    refundId = "",       // auto-generated later
                    reason = reason,
                    refundDetail = detail,
                    requestTime = System.currentTimeMillis(),
                    status = "pending"
                )

                repository.createRefund(refund)   // auto ID generated in DAO
                loadRefunds()

            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }


    // READ (Get all)
    fun loadRefunds() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _refundList.value = repository.getAllRefunds()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    // READ (ID)
    fun loadRefundById(id: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val result = repository.getRefundById(id)
                _refund.value = result

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    // UPDATE
    fun updateRefund(id: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            try {
                repository.updateRefund(id, updates)
                loadRefunds()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // DELETE
    fun deleteRefund(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteRefund(id)
                loadRefunds()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun observeRefunds() {
        repository.listenRefunds { newList ->
            _refundList.value = newList
        }
    }

}

/*
@Composable
fun RefundDetailScreen(refundId: String, viewModel: RefundViewModel = viewModel()) {

    LaunchedEffect(refundId) {
        viewModel.loadRefundById(refundId)
    }

    val refund = viewModel.refund.value
    val loading = viewModel.loading.value
    val error = viewModel.error.value

    when {
        loading -> Text("Loading refund...")
        error != null -> Text("Error: $error")
        refund != null -> {
            Text("Refund ID: ${refund.refundId}")
            Text("Reason: ${refund.reason}")
            Text("Status: ${refund.status}")
        }
    }
}*/
