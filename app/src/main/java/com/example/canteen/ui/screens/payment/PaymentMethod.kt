package com.example.canteen.ui.screens.payment

import android.widget.Button
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.then
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.text.isDigitsOnly
import com.example.canteen.ui.theme.gray
import com.example.canteen.ui.theme.lightBlue
import com.example.canteen.ui.theme.lightRed
import kotlin.math.sin

@Composable
fun PaymentMethod(
    savedCard: String? = null,          // <-- ONLY ONE CARD
    onCardSelected: () -> Unit = {}  // Navigate to Card Detail Page
) {

    var selectedMethod by remember { mutableStateOf<String?>(null) }
    var phoneNumber by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {

        Text(
            "Select Payment Method",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(12.dp))

        // ------------------- CREDIT / DEBIT CARD -------------------
        PaymentOptionCard(
            title = "Credit/Debit Card",
            icon = Icons.Outlined.CreditCard,
            selected = selectedMethod == "card",
            onClick = {
                selectedMethod = if (selectedMethod == "card") null else "card"
            }
        )

        AnimatedVisibility(visible = selectedMethod == "card") {
            Column(modifier = Modifier.padding(start = 12.dp, top = 10.dp)) {

                // Show saved card if exists
                if (savedCard != null) {

                    Text("Saved Card", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))

                    // Example UI for saved card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = lightBlue
                        )
                    ) {
                        Column(Modifier.padding(16.dp).fillMaxWidth()) {
                            Row {
                                Icon(
                                    imageVector = Icons.Filled.CheckBox,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(savedCard, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                //Text("Tap to use this card", fontSize = 14.sp, color = gray)
                            }
                        }
                    }
                } else {
                    // No saved card → Allow user to add one
                    Text("No saved card found.")
                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = onCardSelected, // navigate
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(8.dp)
                    ) {
                        Text("Add New Card")
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ------------------- E-WALLET -------------------
        PaymentOptionCard(
            title = "E-Wallet",
            icon = Icons.Outlined.Wallet,
            selected = selectedMethod == "ewallet",
            onClick = {
                selectedMethod = if (selectedMethod == "ewallet") null else "ewallet"
            }
        )

        // Expand section for E-Wallet
        AnimatedVisibility(visible = selectedMethod == "ewallet") {
            val isValid = remember(phoneNumber) { isValidPhoneNumber(phoneNumber) }
            val showError = phoneNumber.isNotEmpty() && !isValid

            Column(modifier = Modifier.padding(top = 12.dp)) {

                Text("E-Wallet Phone Number")

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    placeholder = { Text("0123456789") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = isValid,
                    singleLine = true
                )
                if (showError) {
                    Text(
                        text = "Invalid phone number format",
                        color = lightRed,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
            }
        }
    }
}

fun isValidPhoneNumber(number: String): Boolean {
    val malaysiaRegex = Regex("^01[0-9]{8,9}$")

    return malaysiaRegex.matches(number)
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

            Text(title, fontSize = 18.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentOptionPreview() {
    CanteenTheme {
        PaymentMethod("Visa ending 4321")
    }
}
