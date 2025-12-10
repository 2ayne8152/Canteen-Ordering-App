package com.example.canteen.viewmodel.payment

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State

class PaymentMethodViewModel : ViewModel() {

    // "card", "ewallet", or null
    private val _selectedMethod = mutableStateOf<String?>(null)
    val selectedMethod: State<String?> = _selectedMethod

    fun select(method: String?) {
        _selectedMethod.value = method
    }
}
