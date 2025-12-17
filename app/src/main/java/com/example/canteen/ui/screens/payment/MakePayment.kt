package com.example.canteen.ui.screens.payment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.canteen.ui.screens.CanteenScreen
import com.example.canteen.ui.theme.CanteenTheme
import com.example.canteen.viewmodel.login.UserViewModel
import com.example.canteen.viewmodel.payment.CardDetailViewModel
import com.example.canteen.viewmodel.payment.ReceiptViewModel
import com.example.canteen.viewmodel.payment.RefundViewModel
import com.example.canteen.viewmodel.usermenu.CartViewModel
import com.example.canteen.viewmodel.usermenu.OrderViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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

        val orderId = remember {
            Firebase.firestore
            .collection("receipt")
            .document()
            .id
        }

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

                // CREATE ORDER
                orderViewModel.createOrder(userId, items, total)

                // CREATE RECEIPT
                receiptViewModel.createReceipt(
                    orderId = orderViewModel.latestOrder.value?.orderId ?: "",
                    selectedMethod!!,
                    total
                )

                scope.launch {
                    snackbarHostState.showSnackbar("Payment successful 🎉")
                    onClick()
                }

                cartViewModel.clearCart()
            }
        )

    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun MakePaymentPreview() {
    CanteenTheme {
        //MakePayment()
    }
}