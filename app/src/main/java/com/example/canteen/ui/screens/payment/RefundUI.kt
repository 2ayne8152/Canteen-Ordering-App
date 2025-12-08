package com.example.canteen.ui.screens.payment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.canteen.ui.theme.CanteenTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Refund(onBack: () -> Unit = {}, onSubmit: () -> Unit = {}) {

    // Reason dropdown
    val refundReasons = listOf(
        "Payment Problem",
        "Technical Problem",
        "Change of Mind",
        "Other"
    )

    var expanded by remember { mutableStateOf(false) }
    var selectedReason by remember { mutableStateOf("") }

    // Other inputs
    var refundDetails by remember { mutableStateOf("") }
    var textFieldWidth by remember { mutableStateOf(0.dp) }
    val isValid = selectedReason.isNotBlank() && refundDetails.isNotBlank()


    Scaffold(
        topBar = {
            Surface(shadowElevation = 6.dp) {
                TopAppBar(
                    title = { Text("Refund Request") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Column {

                Text(
                    text = "Select Refund Reason",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 25.sp
                )

                Spacer(Modifier.height(8.dp))

                // Dropdown List
                Box {
                    val density = LocalDensity.current
                    OutlinedTextField(
                        value = selectedReason,
                        onValueChange = {},
                        label = { Text("Refund Reason") },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                Modifier.clickable { expanded = true }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable{expanded = true}
                            .onGloballyPositioned { coordinates ->
                                textFieldWidth = with(density) {
                                    coordinates.size.width.toDp()
                                }
                            }

                    )

                    DropdownMenu(
                        modifier = Modifier.width(textFieldWidth) ,
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        refundReasons.forEach { reason ->
                            DropdownMenuItem(
                                text = { Text(reason) },
                                onClick = {
                                    selectedReason = reason
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Refund Details
                OutlinedTextField(
                    value = refundDetails,
                    onValueChange = { refundDetails = it },
                    label = { Text("Refund Details") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 5
                )

                Spacer(Modifier.height(24.dp))

                // Submit Button
                Button(
                    onClick = onSubmit,
                    enabled = isValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Submit")
                }
            }
        }
    }
}




@Preview(showBackground = true)
@Composable
fun RefundPreview() {
    CanteenTheme {
        Refund()
    }
}