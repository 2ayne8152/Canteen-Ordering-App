package com.example.canteen.ui.screens.payment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.canteen.ui.screens.CanteenScreen
import com.example.canteen.ui.theme.CanteenTheme
import com.example.canteen.viewmodel.login.UserViewModel
import com.example.canteen.viewmodel.payment.CardDetailViewModel
import com.example.canteen.viewmodel.payment.ReceiptViewModel
import com.example.canteen.viewmodel.payment.RefundViewModel


@Composable
fun MakePayment(
    receiptViewModel: ReceiptViewModel = viewModel(),
    cardDetailViewModel : CardDetailViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
){
    //val savedCard by cardDetailViewModel.savedCard.collectAsState()
    var selectedMethod by remember { mutableStateOf<String?>(null) }
    val user by userViewModel.selectedUser.collectAsState()
    // need to get the userId from the canteenScreen NavHost
    val userId = "AMyzJmi2PrhlSz9jVNx7UJTlCeh2"

    LaunchedEffect(userId) {
        userId.let {
            userViewModel.loadUserById(it)
        }
    }

    Surface {
        Column {
            PaymentMethod(
                cardDetailViewModel = cardDetailViewModel,
                phoneNumber = user?.PhoneNumber ?: "",
                onCardSelected = {},
                savedCard = "2234",
                onMethodSelected = {
                    selectedMethod = it
                }
            )

            Button(
                onClick = {
                    receiptViewModel.createReceipt("O0006", selectedMethod!!, 10.00)
                },
                modifier = Modifier,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Submit")
            }

            Refund(receiptViewModel = receiptViewModel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MakePaymentPreview() {
    CanteenTheme {
        MakePayment()
    }
}