package com.example.canteen.ui.screens.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.canteen.ui.theme.CanteenTheme
import com.example.canteen.ui.theme.gray
import com.example.canteen.ui.theme.middleGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayByCard(onBack: () -> Unit = {}) {

    var cardNumber by remember { mutableStateOf(TextFieldValue()) }
    var expiry by remember { mutableStateOf(TextFieldValue()) }
    var cvv by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }

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
                },
                label = { Text("Card number") },
                placeholder = {Text(text = "1234 5678 9012 3456", color = middleGray)},
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // MM/YY + CVV Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                OutlinedTextField(
                    value = expiry,
                    onValueChange = { input ->
                        val digits = input.text.filter(Char::isDigit).take(4)
                        val formatted = digits.chunked(2).joinToString("/")

                        expiry = input.copy(
                            text = formatted,
                            selection = TextRange(formatted.length)
                        )
                    },
                    label = { Text("Expiry Date") },
                    placeholder = {Text(text = "MM/YY", color = middleGray)},
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = cvv,
                    onValueChange = { cvv = it },
                    label = { Text("CVV") },
                    placeholder = {Text(text = "123", color = middleGray)},
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true
                )
            }

            Spacer(Modifier.height(16.dp))

            // Card Holder Name
            OutlinedTextField(
                value = cardHolder,
                onValueChange = { cardHolder = it },
                label = { Text("Card holder name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(24.dp))

            // Done Button
            Button(
                shape = RoundedCornerShape(8.dp),
                onClick = { /* Handle Save */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Text("Done")
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CanteenTheme() {
        PayByCard()
    }
}