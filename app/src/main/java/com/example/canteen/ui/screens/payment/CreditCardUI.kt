package com.example.canteen.ui.screens.payment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.canteen.ui.theme.AppColors
import com.example.canteen.ui.theme.CanteenTheme
import com.example.canteen.ui.theme.middleGray
import com.example.canteen.ui.theme.lightRed
import com.example.canteen.data.CardDetail
import com.example.canteen.viewmodel.payment.CardDetailViewModel
import androidx.compose.ui.graphics.Color

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayByCard(
    onValidityChange: (Boolean) -> Unit
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

    var hasFocusOnHolder by remember { mutableStateOf(false) }
    var hasTouchedHolder by remember { mutableStateOf(false) }

    val isValid = !isExpiryError && !isCardNumberError && !isCVVError && cardHolder.isNotBlank() && cardNumber.text.isNotEmpty() && expiry.text.isNotEmpty() && cvv.text.isNotEmpty()

    LaunchedEffect(isValid) {
        onValidityChange(isValid)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Card Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 24.sp,
                    color = AppColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(20.dp))

                // Card Number Input
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { newValue ->
                        val digits = newValue.text.filter(Char::isDigit).take(16)
                        val formatted = digits.chunked(4).joinToString(" ")

                        cardNumber = newValue.copy(
                            text = formatted,
                            selection = TextRange(formatted.length)
                        )

                        if (digits.length == 16) {
                            isCardNumberError = !isValidCardNumber(cardNumber.text)
                        } else {
                            isCardNumberError = false
                        }
                    },
                    label = { Text("Card number", color = AppColors.textSecondary) },
                    placeholder = { Text(text = "1234 5678 9012 3456", color = AppColors.textTertiary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused && hasFocusOnCard) {
                                isCardNumberError = !isValidCardNumber(cardNumber.text)
                            }
                            hasFocusOnCard = focusState.isFocused
                        },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = isCardNumberError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.primary,
                        unfocusedBorderColor = AppColors.divider,
                        focusedTextColor = AppColors.textPrimary,
                        unfocusedTextColor = AppColors.textPrimary,
                        cursorColor = AppColors.primary,
                        errorBorderColor = AppColors.error,
                        errorCursorColor = AppColors.error
                    ),
                    trailingIcon = {
                        if (hasFocusOnCard && cardNumber.text.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    cardNumber = TextFieldValue("")
                                    isCardNumberError = false
                                }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear",
                                    tint = AppColors.textSecondary
                                )
                            }
                        }
                    }
                )
                if (isCardNumberError) {
                    Text(
                        "Invalid card number",
                        color = AppColors.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // MM/YY + CVV Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                                isExpiryError = false
                            }
                        },
                        label = { Text("Expiry", color = AppColors.textSecondary) },
                        placeholder = { Text(text = "MM/YY", color = AppColors.textTertiary) },
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused && hasFocusOnExpiry) {
                                    isExpiryError = !isValidExpiry(expiry.text)
                                }
                                hasFocusOnExpiry = focusState.isFocused
                            },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        isError = isExpiryError,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.primary,
                            unfocusedBorderColor = AppColors.divider,
                            focusedTextColor = AppColors.textPrimary,
                            unfocusedTextColor = AppColors.textPrimary,
                            cursorColor = AppColors.primary,
                            errorBorderColor = AppColors.error,
                            errorCursorColor = AppColors.error
                        ),
                        supportingText = {
                            if (isExpiryError) {
                                Text(
                                    "Invalid date",
                                    color = AppColors.error,
                                    fontSize = 11.sp
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
                                        contentDescription = "Clear",
                                        tint = AppColors.textSecondary
                                    )
                                }
                            }
                        }
                    )

                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { newInput ->
                            val digits = newInput.text.filter(Char::isDigit).take(3)

                            cvv = newInput.copy(
                                text = digits,
                                selection = TextRange(digits.length)
                            )

                            if (digits.length == 3) {
                                isCVVError = !isValidCVV(cvv.text)
                            } else {
                                isCVVError = false
                            }
                        },
                        label = { Text("CVV", color = AppColors.textSecondary) },
                        placeholder = { Text(text = "123", color = AppColors.textTertiary) },
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused && hasFocusOnCVV) {
                                    isCVVError = !isValidCVV(cvv.text)
                                }
                                hasFocusOnCVV = focusState.isFocused
                            },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        isError = isCVVError,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.primary,
                            unfocusedBorderColor = AppColors.divider,
                            focusedTextColor = AppColors.textPrimary,
                            unfocusedTextColor = AppColors.textPrimary,
                            cursorColor = AppColors.primary,
                            errorBorderColor = AppColors.error,
                            errorCursorColor = AppColors.error
                        ),
                        supportingText = {
                            if (isCVVError) {
                                Text(
                                    "Invalid CVV",
                                    color = AppColors.error,
                                    fontSize = 11.sp
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
                                        contentDescription = "Clear",
                                        tint = AppColors.textSecondary
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
                    label = { Text("Cardholder name", color = AppColors.textSecondary) },
                    placeholder = { Text("JOHN DOE", color = AppColors.textTertiary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                hasTouchedHolder = true
                            }
                            hasFocusOnHolder = focusState.isFocused
                        },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.primary,
                        unfocusedBorderColor = AppColors.divider,
                        focusedTextColor = AppColors.textPrimary,
                        unfocusedTextColor = AppColors.textPrimary,
                        cursorColor = AppColors.primary
                    ),
                    trailingIcon = {
                        if (cardHolder.isNotEmpty() && hasFocusOnHolder) {
                            IconButton(
                                onClick = {
                                    cardHolder = ""
                                }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear",
                                    tint = AppColors.textSecondary
                                )
                            }
                        }
                    },
                    supportingText = {
                        if (cardHolder.isEmpty() && hasTouchedHolder && !hasFocusOnHolder) {
                            Text(
                                "Cardholder name is required",
                                color = AppColors.error,
                                fontSize = 11.sp
                            )
                        }
                    }
                )
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
    if (!expiry.matches(Regex("""^(0[1-9]|1[0-2])/\d{2}$"""))) return false

    val month = expiry.substring(0, 2).toInt()
    val year = "20" + expiry.substring(3, 5)

    val expYear = year.toInt()

    val now = java.time.YearMonth.now()
    val cardDate = java.time.YearMonth.of(expYear, month)

    return !cardDate.isBefore(now)
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CanteenTheme {
        //PayByCard()
    }
}