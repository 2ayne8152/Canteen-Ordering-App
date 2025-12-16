package com.example.canteen.ui.screens.payment

import android.R.attr.text
import android.os.Build
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.canteen.ui.theme.CanteenTheme
import com.example.canteen.ui.theme.black
import com.example.canteen.ui.theme.blue
import com.example.canteen.ui.theme.veryLightBlue
import com.example.canteen.ui.theme.white
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.canteen.ui.theme.gray
import com.example.canteen.ui.theme.lightBlue
import com.example.canteen.ui.theme.lightRed
import com.example.canteen.viewmodel.payment.CardDetailViewModel
import com.example.canteen.viewmodel.payment.PaymentMethodViewModel
import kotlin.math.sin
import androidx.compose.ui.graphics.Color

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PaymentMethod(
    paymentMethodViewModel: PaymentMethodViewModel = viewModel(),
    phoneNumber: String,
    onMethodSelected: (String?) -> Unit,
    onCardValidityChange: (Boolean) -> Unit
) {
    val selectedMethod = paymentMethodViewModel.selectedMethod.value

    Column {
        Text(
            text = "Select Payment Method",
            style = MaterialTheme.typography.titleMedium,
            fontSize = 25.sp, color = Color.Black
        )

        Spacer(Modifier.height(12.dp))

        // ------------------- CREDIT / DEBIT CARD -------------------
        PaymentOptionCard(
            title = "Credit/Debit Card",
            icon = Icons.Outlined.CreditCard,
            selected = selectedMethod == "Card",
            onClick = {
                //selectedMethod = if (selectedMethod == "card") null else "card"
                val newMethod = if (selectedMethod == "Card") null else "Card"
                paymentMethodViewModel.select(newMethod)
                onMethodSelected(newMethod)
            }
        )


        Spacer(Modifier.height(16.dp))

        // ------------------- E-WALLET -------------------
        PaymentOptionCard(
            title = "E-Wallet",
            icon = Icons.Outlined.Wallet,
            selected = selectedMethod == "E-wallet",
            onClick = {
                //selectedMethod = if (selectedMethod == "ewallet") null else "ewallet"
                val newMethod = if (selectedMethod == "E-wallet") null else "E-wallet"
                paymentMethodViewModel.select(newMethod)
                onMethodSelected(newMethod)
            }
        )
        AnimatedVisibility(visible = selectedMethod == "Card") {
            PayByCard(
                onValidityChange = onCardValidityChange
            )
        }

        // Expand section for E-Wallet
        AnimatedVisibility(visible = selectedMethod == "E-wallet") {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                val formattedNumber = "${phoneNumber.substring(0, 3)}-${phoneNumber.substring(3, 6)} ${phoneNumber.substring(6)}"

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "  E-Wallet Phone Number",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 22.sp, color = Color.Black
                )

                Spacer(Modifier.height(12.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = lightBlue
                    )
                ) {
                    Column(Modifier
                        .padding(16.dp)
                        .fillMaxWidth()) {
                        Row {
                            Icon(
                                imageVector = Icons.Filled.CheckBox,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(text = formattedNumber, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                            //Text("Tap to use this card", fontSize = 14.sp, color = gray)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun PaymentOptionCard(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) blue else black
    val backgroundColor = if (selected) veryLightBlue else white

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            RadioButton(
                selected = selected,
                onClick = { onClick() }
            )

            Spacer(Modifier.width(10.dp))

            Icon(icon, contentDescription = null)

            Spacer(Modifier.width(10.dp))

            Text(title, fontSize = 18.sp, color = Color.Black)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PaymentOptionPreview() {
    CanteenTheme {
        //PaymentMethod(savedCard = "Visa ending 4321", phoneNumber = "0123456789", cardDetailViewModel = viewModel())
    }
}
