package com.example.canteen.ui.screens.payment

import android.app.AlertDialog
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import com.example.canteen.ui.theme.AppColors
import com.example.canteen.ui.theme.gray
import com.example.canteen.viewmodel.usermenu.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Refund(
    onBack: () -> Unit = {},
    refundViewModel: RefundViewModel,
    receiptViewModel: ReceiptViewModel,
    orderViewModel: OrderViewModel
) {
    val loading by refundViewModel.loading.collectAsState()
    val error by refundViewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val receiptPair by receiptViewModel.receiptLoadByOrderId.collectAsState()
    val newRefundId by refundViewModel.newRefundId.collectAsState()

    // UI States
    val refundReasons = listOf("Payment Problem", "Technical Problem", "Change of Mind", "Other")

    var expanded by remember { mutableStateOf(false) }
    var selectedReason by remember { mutableStateOf("") }
    var refundDetails by remember { mutableStateOf("") }
    var textFieldWidth by remember { mutableStateOf(0.dp) }
    var hasTouchedHolder by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val isValid = selectedReason.isNotBlank() && refundDetails.isNotBlank()

    // Error Snackbar
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar("Error: $it")
            refundViewModel.clearError()
        }
    }

    LaunchedEffect(newRefundId, receiptPair) {
        val receiptId = receiptPair?.first?.receiptId
        val refundId = newRefundId

        if (!receiptId.isNullOrBlank() && !refundId.isNullOrBlank()) {
            receiptViewModel.updateReceipt(
                id = receiptId,
                updates = mapOf("refundId" to refundId)
            )
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

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column {

            // --- HEADER ----
            Text(
                text = "Select Refund Reason",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 25.sp, color = Color.White
            )

            Spacer(Modifier.height(8.dp))

            // --- DROPDOWN ----
            Box {
                val density = LocalDensity.current
                OutlinedTextField(
                    value = selectedReason,
                    onValueChange = {},
                    label = { Text("Refund Reason", color = Color.White) },
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
                        },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.primary,
                        unfocusedBorderColor = AppColors.divider,
                        focusedTextColor = AppColors.textPrimary,
                        unfocusedTextColor = AppColors.textPrimary,
                        cursorColor = AppColors.primary,
                        errorBorderColor = AppColors.error,
                        errorCursorColor = AppColors.error
                    )
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { expanded = true }
                )

                DropdownMenu(
                    modifier = Modifier.width(textFieldWidth).background(AppColors.surface),
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    refundReasons.forEach { reason ->
                        DropdownMenuItem(
                            text = { Text(reason, color = AppColors.textPrimary) },
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
                label = { Text("Refund Details", color = Color.White) },
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
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.primary,
                    unfocusedBorderColor = AppColors.divider,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    cursorColor = AppColors.primary,
                    errorBorderColor = AppColors.error,
                    errorCursorColor = AppColors.error
                )
            )

            Spacer(Modifier.height(24.dp))

            // --- SUBMIT BUTTON ---
            Button(
                onClick = { showConfirmDialog = true },
                enabled = isValid && !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                elevation = ButtonDefaults.buttonElevation(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.primary,
                    disabledContainerColor = AppColors.disabled
                )
            ) {
                Text(
                    "Submit",
                    color = AppColors.surface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- LOADING DIALOG ---
        if (loading) {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                title = { Text("Submitting...", color = Color.White) }
            )
        }

        // --- CONFIRMATION DIALOG ---
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                icon = {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = AppColors.primary,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        "Submit Refund Request",
                        color = AppColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Are you sure you want to submit this refund request?",
                            color = AppColors.textSecondary,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        HorizontalDivider(color = AppColors.divider)

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Order #:",
                                color = AppColors.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                receiptPair?.first?.orderId?.takeLast(6) ?: "N/A",
                                color = AppColors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                "Reason:",
                                color = AppColors.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                selectedReason,
                                color = AppColors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                "Details:",
                                color = AppColors.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                refundDetails,
                                color = AppColors.textPrimary,
                                fontSize = 14.sp,
                                maxLines = 3,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                "Amount:",
                                color = AppColors.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "RM ${"%.2f".format(receiptPair?.first?.pay_Amount ?: 0.0)}",
                                color = AppColors.primary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmDialog = false
                            val orderId = receiptPair?.first?.orderId

                            if (!orderId.isNullOrBlank()) {
                                refundViewModel.createRefund(selectedReason, refundDetails)
                                orderViewModel.orderStatusUpdate(orderId, "REFUNDED")
                                onBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirm", color = AppColors.surface)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showConfirmDialog = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = AppColors.textPrimary
                        )
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = AppColors.surface,
                shape = RoundedCornerShape(20.dp)
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