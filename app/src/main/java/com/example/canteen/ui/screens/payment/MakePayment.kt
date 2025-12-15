package com.example.canteen.ui.screens.payment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakePayment(
    receiptViewModel: ReceiptViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    onBack: () -> Unit = {}
){
    //val savedCard by cardDetailViewModel.savedCard.collectAsState()
    var selectedMethod by remember { mutableStateOf<String?>("Card") }
    var isCardValid by remember { mutableStateOf(false) }

    val user by userViewModel.selectedUser.collectAsState()
    // need to get the userId from the canteenScreen NavHost
    val userId = "AMyzJmi2PrhlSz9jVNx7UJTlCeh2"
    val isSubmitEnabled = when (selectedMethod) {
        "Card" -> isCardValid
        "E-wallet" -> true
        else -> false
    }

    LaunchedEffect(userId) {
        userId.let {
            userViewModel.loadUserById(it)
        }
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 6.dp) {
                TopAppBar(
                    title = { Text("Complete your payment") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        },
        bottomBar = {
            PaymentBottomBar(
                itemCount = 3,
                totalAmount = 10.00,
                enabled = isSubmitEnabled,
                onSubmit = {
                    receiptViewModel.createReceipt(
                        "O0006",
                        selectedMethod!!,
                        10.00
                    )
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            PaymentMethod(
                phoneNumber = user?.PhoneNumber ?: "",
                onMethodSelected = { selectedMethod = it },
                onCardValidityChange = { isCardValid = it }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun MakePaymentPreview() {
    CanteenTheme {
        MakePayment()
    }
}