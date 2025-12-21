package com.example.canteen.ui.screens.payment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.canteen.ui.screens.CanteenScreen
import com.example.canteen.ui.theme.AppColors
import com.example.canteen.ui.theme.CanteenTheme
import com.example.canteen.viewmodel.login.UserViewModel
import com.example.canteen.viewmodel.payment.CardDetailViewModel
import com.example.canteen.viewmodel.payment.ReceiptViewModel
import com.example.canteen.viewmodel.payment.RefundViewModel
import com.example.canteen.viewmodel.usermenu.CartViewModel
import com.example.canteen.viewmodel.usermenu.OrderViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakePayment(
    receiptViewModel: ReceiptViewModel,
    userViewModel: UserViewModel,
    onBack: () -> Unit = {},
    onClick: () -> Unit,
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModel
) {
    var selectedMethod by remember { mutableStateOf<String?>("Card") }
    var isCardValid by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val user by userViewModel.selectedUser.collectAsState()
    val cart = cartViewModel.cart.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = AppColors.background,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = AppColors.success,
                        contentColor = AppColors.surface,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppColors.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 140.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                PaymentMethod(
                    phoneNumber = user?.PhoneNumber ?: "",
                    onMethodSelected = { selectedMethod = it },
                    onCardValidityChange = { isCardValid = it }
                )
            }

            PaymentBottomBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                itemCount = cart.value.sumOf { it.quantity },
                totalAmount = cart.value.sumOf { it.totalPrice },
                enabled = when (selectedMethod) {
                    "Card" -> isCardValid && !isProcessing
                    "E-wallet" -> !isProcessing
                    else -> false
                },
                onSubmit = {
                    // Show confirmation dialog instead of processing immediately
                    showConfirmDialog = true
                }
            )
        }

        // Payment Confirmation Dialog
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                icon = {
                    Icon(
                        Icons.Default.Payment,
                        contentDescription = null,
                        tint = AppColors.primary,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        "Confirm Payment",
                        color = AppColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Are you sure you want to complete this payment?",
                            color = AppColors.textSecondary,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        HorizontalDivider(color = AppColors.divider)

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Items:",
                                color = AppColors.textSecondary,
                                fontSize = 14.sp
                            )
                            Text(
                                "${cart.value.sumOf { it.quantity }} items",
                                color = AppColors.textPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Method:",
                                color = AppColors.textSecondary,
                                fontSize = 14.sp
                            )
                            Text(
                                selectedMethod ?: "",
                                color = AppColors.textPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Total:",
                                color = AppColors.textSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "RM ${"%.2f".format(cart.value.sumOf { it.totalPrice })}",
                                color = AppColors.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmDialog = false
                            isProcessing = true

                            val userId = user?.UserID ?: return@Button
                            val items = cart.value
                            val total = cart.value.sumOf { it.totalPrice }

                            // CREATE ORDER & RECEIPT
                            scope.launch {
                                try {
                                    val order = orderViewModel.createOrder(userId, items, total)
                                    receiptViewModel.createReceipt(
                                        orderId = order.orderId,
                                        selectedMethod!!,
                                        total
                                    )

                                    cartViewModel.clearCart()  // clear cart immediately

                                    // show success snackbar
                                    snackbarHostState.showSnackbar("Payment successful 🎉")
                                    isProcessing = false
                                    onClick()  // navigate back or update UI

                                } catch (e: Exception) {
                                    // Stock validation failed or other error
                                    snackbarHostState.showSnackbar(
                                        message = e.message ?: "Payment failed. Please try again.",
                                        duration = SnackbarDuration.Long
                                    )
                                    isProcessing = false

                                    // Don't clear cart - let user adjust quantities
                                    return@launch
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirm Payment", color = AppColors.surface)
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

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun MakePaymentPreview() {
    CanteenTheme {
        //MakePayment()
    }
}