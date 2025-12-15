package com.example.canteen.ui.screens.payment

import android.app.AlertDialog
import android.util.Log
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.canteen.ui.theme.CanteenTheme
import com.example.canteen.ui.theme.lightRed
import com.example.canteen.viewmodel.payment.ReceiptViewModel
import com.example.canteen.viewmodel.payment.RefundViewModel
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Refund(
    onBack: () -> Unit = {},
    refundViewModel: RefundViewModel = viewModel(),
    receiptViewModel: ReceiptViewModel
) {
    val loading by refundViewModel.loading.collectAsState()
    val error by refundViewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val newReceiptId by receiptViewModel.newReceiptId.collectAsState()
    val newRefundId by refundViewModel.newRefundId.collectAsState()

    // UI States
    val refundReasons = listOf("Payment Problem", "Technical Problem", "Change of Mind", "Other")

    var expanded by remember { mutableStateOf(false) }
    var selectedReason by remember { mutableStateOf("") }
    var refundDetails by remember { mutableStateOf("") }
    var textFieldWidth by remember { mutableStateOf(0.dp) }
    var hasTouchedHolder by remember { mutableStateOf(false) }

    val isValid = selectedReason.isNotBlank() && refundDetails.isNotBlank()

    // Error Snackbar
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar("Error: $it")
            refundViewModel.clearError()
        }
    }

    LaunchedEffect(Unit) {
        refundViewModel.refundCreated.collect { refundId ->

            val receiptId = newReceiptId
            if (receiptId != null) {
                receiptViewModel.updateReceipt(
                    id = receiptId,
                    updates = mapOf("refundId" to newRefundId)
                )
            } else {
                Log.e("Refund", "newReceiptId is NULL!")
            }
        }
    }


    // Success Snackbar
    LaunchedEffect(refundViewModel.refundCreated.collectAsState().value) {
        if (refundViewModel.refundCreated.value) {
            snackbarHostState.showSnackbar("Refund request submitted")

            // Reset form
            selectedReason = ""
            refundDetails = ""
            hasTouchedHolder = false

            refundViewModel.resetCreatedFlag()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = { Text("Refund Request") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
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

                // --- HEADER ----
                Text(
                    text = "Select Refund Reason",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 25.sp, color = Color.Black
                )

                Spacer(Modifier.height(8.dp))

                // --- DROPDOWN ----
                Box {
                    val density = LocalDensity.current
                    OutlinedTextField(
                        value = selectedReason,
                        onValueChange = {},
                        label = { Text("Refund Reason", color = Color.Black) },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                if (expanded) Icons.Default.KeyboardArrowUp
                                else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                textFieldWidth = with(density) { coordinates.size.width.toDp() }
                            }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { expanded = true }
                    )

                    DropdownMenu(
                        modifier = Modifier.width(textFieldWidth),
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        refundReasons.forEach { reason ->
                            DropdownMenuItem(
                                text = { Text(reason, color = Color.Black) },
                                onClick = {
                                    selectedReason = reason
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // --- DETAILS FIELD ---
                OutlinedTextField(
                    value = refundDetails,
                    onValueChange = { refundDetails = it },
                    label = { Text("Refund Details", color = Color.Black) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) hasTouchedHolder = true
                        },
                    maxLines = 5,
                    supportingText = {
                        if (refundDetails.isEmpty() && hasTouchedHolder) {
                            Text("Cannot be empty", color = lightRed, fontSize = 12.sp)
                        }
                    }
                )

                Spacer(Modifier.height(24.dp))

                // --- SUBMIT BUTTON ---
                Button(
                    onClick = {
                        refundViewModel.createRefund(selectedReason, refundDetails)
                    },
                    enabled = isValid && !loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    Text("Submit", color = Color.Black)
                }
            }
        }

        // --- LOADING DIALOG ---
        if (loading) {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                title = { Text("Submitting...", color = Color.Black) }
            )
        }
    }
}





@Preview(showBackground = true)
@Composable
fun RefundPreview() {
    CanteenTheme {
        //Refund()
    }
}