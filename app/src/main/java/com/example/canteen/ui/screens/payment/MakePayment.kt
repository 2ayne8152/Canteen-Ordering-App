package com.example.canteen.ui.screens.payment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.canteen.viewmodel.login.UserViewModel
import com.example.canteen.viewmodel.payment.ReceiptViewModel
import com.example.canteen.viewmodel.usermenu.CartViewModel
import com.example.canteen.viewmodel.usermenu.OrderViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakePayment(
    receiptViewModel: ReceiptViewModel,
    userViewModel: UserViewModel,
    onBack: () -> Unit = {},
    onClick: () -> Unit,
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModel
) {
    var selectedMethod by remember { mutableStateOf<String?>("Card") }
    var isCardValid by remember { mutableStateOf(false) }

    val user by userViewModel.selectedUser.collectAsState()
    val cart = cartViewModel.cart.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            PaymentMethod(
                phoneNumber = user?.PhoneNumber ?: "",
                onMethodSelected = { selectedMethod = it },
                onCardValidityChange = { isCardValid = it }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 130.dp)
        )

        PaymentBottomBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            itemCount = cart.value.sumOf { it.quantity },
            totalAmount = cart.value.sumOf { it.totalPrice },
            enabled = when (selectedMethod) {
                "Card" -> isCardValid
                "E-wallet" -> true
                else -> false
            },
            onSubmit = {
                val userId = user?.UserID ?: return@PaymentBottomBar
                val items = cart.value
                val total = cart.value.sumOf { it.totalPrice }

                // Use coroutine scope to launch suspend operations
                scope.launch {
                    try {
                        // CREATE ORDER (with remainQuantity updated)
                        val order = orderViewModel.createOrder(userId, items, total)

                        // CREATE RECEIPT using the returned order
                        receiptViewModel.createReceipt(
                            orderId = order.orderId,
                            selectedMethod!!,
                            total
                        )

                        snackbarHostState.showSnackbar("Payment successful 🎉")
                        onClick()
                        cartViewModel.clearCart()
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Payment failed: ${e.message}")
                    }
                }
            }
        )
    }
}
