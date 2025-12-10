package com.example.canteen.ui.screens.payment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.substring
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.canteen.ui.theme.CanteenTheme
import com.example.canteen.ui.theme.middleGray
import com.example.canteen.ui.theme.lightRed
import com.example.canteen.viewmodel.payment.CardDetail
import com.example.canteen.viewmodel.payment.CardDetailViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayByCard(
    onBack: () -> Unit = {},
    viewModel: CardDetailViewModel
) {
    val focusManager = LocalFocusManager.current

    var cardNumber by remember { mutableStateOf(TextFieldValue()) }
    var expiry by remember { mutableStateOf(TextFieldValue()) }
    var cvv by remember { mutableStateOf(TextFieldValue()) }
    var cardHolder by remember { mutableStateOf("") }

    var isCardNumberError by remember { mutableStateOf(false) }
    var hasFocusOnCard by remember { mutableStateOf(false) }

    var isCVVError by remember { mutableStateOf(false) }
    var hasFocusOnCVV by remember { mutableStateOf(false) }

    var isExpiryError by remember { mutableStateOf(false) }
    var hasFocusOnExpiry by remember { mutableStateOf(false) }

    var hasFocusOnHolder by remember {mutableStateOf(false)}
    var hasTouchedHolder by remember { mutableStateOf(false) }

    val isValid = !isExpiryError && !isCardNumberError && !isCVVError && cardHolder.isNotBlank() && cardNumber.text.isNotEmpty() && expiry.text.isNotEmpty() && cvv.text.isNotEmpty()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add credit or debit card") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                modifier = Modifier.shadow(6.dp)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    focusManager.clearFocus()
                }
        ) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {

                Text(
                    text = "Card Detail",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 25.sp
                )

                Spacer(Modifier.height(16.dp))

                var showTrailingIcon by remember { mutableStateOf(false) }

                // Card Number Input

                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { newValue ->

                        val digits = newValue.text.filter(Char::isDigit).take(16)
                        val formatted = digits.chunked(4).joinToString(" ")

                        // place cursor at end safely
                        cardNumber = newValue.copy(
                            text = formatted,
                            selection = TextRange(formatted.length)
                        )

                        if (digits.length == 16) {
                            isCardNumberError = !isValidCardNumber(cardNumber.text)
                        } else {
                            // don't show error while typing
                            isCardNumberError = false
                        }
                    },
                    label = { Text("Card number") },
                    placeholder = { Text(text = "1234 5678 9012 3456", color = middleGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused && hasFocusOnCard) {
                                // Validation happens ONLY after user had focus before
                                isCardNumberError = !isValidCardNumber(cardNumber.text)
                            }
                            hasFocusOnCard = focusState.isFocused
                        },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = isCardNumberError,
                    trailingIcon = {
                        if (hasFocusOnCard && cardNumber.text.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    cardNumber = TextFieldValue("")
                                    isCardNumberError = false
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    }
                )
                if (isCardNumberError) {
                    Text(
                        "Invalid card number",
                        color = lightRed,
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                // MM/YY + CVV Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    OutlinedTextField(
                        value = expiry,
                        onValueChange = { input ->
                            val digits = input.text.filter(Char::isDigit).take(4)
                            val formatted = digits.chunked(2).joinToString("/")

                            expiry = input.copy(
                                text = formatted,
                                selection = TextRange(formatted.length)
                            )

                            if (digits.length == 4) {
                                isExpiryError = !isValidExpiry(expiry.text)
                            } else {
                                // don't show error while typing
                                isExpiryError = false
                            }
                        },
                        label = { Text("Expiry Date") },
                        placeholder = { Text(text = "MM/YY", color = middleGray) },
                        modifier = Modifier.weight(1f).onFocusChanged { focusState ->
                            if (!focusState.isFocused && hasFocusOnExpiry) {
                                // Validation happens ONLY after user had focus before
                                isExpiryError = !isValidExpiry(expiry.text)
                            }
                            hasFocusOnExpiry = focusState.isFocused
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        isError = isExpiryError,
                        supportingText = {
                            if (isExpiryError) {
                                Text(
                                    "Invalid or expired date",
                                    color = lightRed,
                                    fontSize = 12.sp
                                )
                            }
                        },
                        trailingIcon = {
                            if (hasFocusOnExpiry && expiry.text.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        expiry = TextFieldValue("")
                                        isExpiryError = false
                                    }) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Clear"
                                    )
                                }
                            }
                        }
                    )

                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { newInput ->

                            val digits = newInput.text.filter(Char::isDigit).take(3)

                            // place cursor at end safely
                            cvv = newInput.copy(
                                text = digits,
                                selection = TextRange(digits.length)
                            )

                            if (digits.length == 3) {
                                isCVVError = !isValidCVV(cvv.text)
                            } else {
                                // don't show error while typing
                                isCVVError = false
                            }
                        },
                        label = { Text("CVV") },
                        placeholder = { Text(text = "123", color = middleGray) },
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { focusState ->
                            if (!focusState.isFocused && hasFocusOnCVV) {
                                // Validation happens ONLY after user had focus before
                                isCVVError = !isValidCVV(cvv.text)
                            }
                            hasFocusOnCVV = focusState.isFocused
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        isError = isCVVError,
                        supportingText = {
                            if (isCVVError) {
                                Text(
                                    "Invalid CVV format",
                                    color = lightRed,
                                    fontSize = 12.sp
                                )
                            }
                        },
                        trailingIcon = {
                            if (hasFocusOnCVV && cvv.text.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        cvv = TextFieldValue("")
                                        isCVVError = false
                                    }) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Clear"
                                    )
                                }
                            }
                        }
                    )

                }

                Spacer(Modifier.height(16.dp))

                // Card Holder Name
                OutlinedTextField(
                    value = cardHolder,
                    onValueChange = { cardHolder = it },
                    label = { Text("Card holder name") },
                    modifier = Modifier.fillMaxWidth().onFocusChanged{ focusState ->
                        if (focusState.isFocused) {
                            hasTouchedHolder = true
                        }
                        hasFocusOnHolder = focusState.isFocused
                    },
                    singleLine = true,
                    trailingIcon = {
                        if (cardHolder.isNotEmpty() && hasFocusOnHolder) {
                            IconButton(
                                onClick = {
                                    cardHolder = ""
                                }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    },
                    supportingText = {
                        if (cardHolder.isEmpty() && hasTouchedHolder) {
                            Text(
                                "Cannot be empty",
                                color = lightRed,
                                fontSize = 12.sp
                            )
                        }
                    }
                )

                Spacer(Modifier.height(24.dp))

                // Done Button
                Button(
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        val card = CardDetail(
                            maskedCard = cardNumber.text.substring(15),
                            expiry = expiry.text,
                            CVV = cvv.text
                        )
                        viewModel.saveCard(card)   // <-- SAVE TO VIEWMODEL

                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    elevation = ButtonDefaults.buttonElevation(8.dp),
                    enabled = isValid
                ) {
                    Text("Done")
                }
            }
        }
    }
}

fun isValidCardNumber(number: String): Boolean {
    val cardRegex = Regex("^\\d{4}\\s\\d{4}\\s\\d{4}\\s\\d{4}$")

    return cardRegex.matches(number)
}

fun isValidCVV(number: String): Boolean {
    val cvvRegex = Regex("^\\d{3}$")

    return cvvRegex.matches(number)
}

@RequiresApi(Build.VERSION_CODES.O)
fun isValidExpiry(expiry: String): Boolean {
    // Must be in MM/YY format
    if (!expiry.matches(Regex("""^(0[1-9]|1[0-2])/\d{2}$"""))) return false

    val month = expiry.substring(0, 2).toInt()
    val year = "20" + expiry.substring(3, 5)   // convert YY → 20YY

    val expYear = year.toInt()

    // Get current month & year
    val now = java.time.YearMonth.now()
    val cardDate = java.time.YearMonth.of(expYear, month)

    return !cardDate.isBefore(now)  // card date must be >= current date
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CanteenTheme {
        PayByCard(viewModel =  viewModel())
    }
}