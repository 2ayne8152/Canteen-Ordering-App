package com.example.canteen.ui.screens.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.canteen.ui.theme.AppColors
import com.example.canteen.ui.theme.CanteenTheme
import com.example.canteen.ui.theme.lightBlue
import com.example.canteen.ui.theme.lightGreen
import com.example.canteen.ui.theme.lightRed
import com.example.canteen.ui.theme.lightViolet
import com.example.canteen.ui.theme.softGreen
import com.example.canteen.ui.theme.veryLightRed
import com.example.canteen.viewmodel.payment.ReceiptViewModel
import com.example.canteen.viewmodel.payment.RefundViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import com.example.canteen.viewmodel.login.UserViewModel
import com.example.canteen.viewmodel.usermenu.OrderViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefundDetailPage(
    orderViewModel: OrderViewModel,
    receiptViewModel: ReceiptViewModel,
    refundViewModel: RefundViewModel,
    userViewModel: UserViewModel,
    onBack: () -> Unit = {}
) {
    val order by orderViewModel.refundOrder.collectAsState()
    val user by userViewModel.selectedUser.collectAsState()
    val selected by receiptViewModel.selectedRefund.collectAsState()
    var responseBy by remember { mutableStateOf("") }
    var responseRemark by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var textFieldWidth by remember { mutableStateOf(0.dp) }
    val isValid = responseBy.isNotBlank() && responseRemark.isNotBlank()

    if (selected == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(AppColors.background)
        ) {
            Text("Loading...", color = AppColors.textPrimary)
        }
        return
    }

    val receipt = selected!!.first
    val refund = selected!!.second

    LaunchedEffect(receipt.orderId) {
        orderViewModel.getOrder(receipt.orderId)
    }

    Scaffold(
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Refund Details",
                        color = AppColors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = AppColors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.surface,
                    titleContentColor = AppColors.textPrimary
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
                .background(AppColors.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // ------------------------ ORDER INFO CARD ------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Order #${receipt.orderId.take(6)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = AppColors.textPrimary
                        )
                        Text(
                            text = formatTime(refund?.requestTime ?: 0L),
                            color = AppColors.textSecondary,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Total: RM${"%.2f".format(receipt.pay_Amount)}",
                        fontWeight = FontWeight.Bold,
                        color = AppColors.primary,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ------------------------ ORDER ITEMS CARD ------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Order Items",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AppColors.textPrimary
                    )

                    Spacer(Modifier.height(12.dp))

                    order?.items?.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.menuItem.name} x${item.quantity}",
                                color = AppColors.textSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "RM ${"%.2f".format(item.totalPrice)}",
                                color = AppColors.textPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ------------------------ REFUND REQUEST CARD ------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Refund Request",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AppColors.textPrimary
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Reason:",
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.textSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        refund?.reason ?: "",
                        fontWeight = FontWeight.Medium,
                        color = AppColors.textPrimary,
                        fontSize = 16.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Details:",
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.textSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        refund?.refundDetail ?: "",
                        lineHeight = 20.sp,
                        color = AppColors.textPrimary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ------------------------ REFUND RESPONSE CARD ------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Refund Response",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AppColors.textPrimary
                    )

                    Spacer(Modifier.height(16.dp))

                    Box {
                        val density = LocalDensity.current
                        OutlinedTextField(
                            value = responseBy,
                            onValueChange = { responseBy = it },
                            label = { Text("Response By", color = AppColors.textSecondary) },
                            singleLine = true,
                            trailingIcon = {
                                Icon(
                                    if (expanded) Icons.Default.KeyboardArrowUp
                                    else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = AppColors.textSecondary,
                                    modifier = Modifier.clickable {
                                        expanded = !expanded
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    textFieldWidth = with(density) { coordinates.size.width.toDp() }
                                },
                            keyboardActions = KeyboardActions(onDone = { expanded = false }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.primary,
                                unfocusedBorderColor = AppColors.divider,
                                focusedTextColor = AppColors.textPrimary,
                                unfocusedTextColor = AppColors.textPrimary,
                                cursorColor = AppColors.primary
                            )
                        )

                        DropdownMenu(
                            modifier = Modifier
                                .width(textFieldWidth)
                                .background(AppColors.surface),
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = user?.Name.orEmpty(),
                                        color = AppColors.textPrimary
                                    )
                                },
                                onClick = {
                                    responseBy = user?.Name.orEmpty()
                                    expanded = false
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = responseRemark,
                        onValueChange = { responseRemark = it },
                        label = { Text("Remark", color = AppColors.textSecondary) },
                        placeholder = { Text("Enter your remark...", color = AppColors.textTertiary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.primary,
                            unfocusedBorderColor = AppColors.divider,
                            focusedTextColor = AppColors.textPrimary,
                            unfocusedTextColor = AppColors.textPrimary,
                            cursorColor = AppColors.primary
                        )
                    )

                    Spacer(Modifier.height(20.dp))

                    // Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                onBack()
                                refundViewModel.updateRefund(
                                    id = receipt.refundId!!,
                                    updates = mapOf(
                                        "refundBy" to responseBy,
                                        "remark" to responseRemark,
                                        "status" to "Approved",
                                    )
                                )
                                orderViewModel.orderStatusUpdate(receipt.orderId, "APPROVED")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.success,
                                disabledContainerColor = AppColors.disabled
                            ),
                            enabled = isValid,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                "Approve",
                                color = AppColors.surface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Button(
                            onClick = {
                                onBack()
                                refundViewModel.updateRefund(
                                    id = receipt.refundId!!,
                                    updates = mapOf(
                                        "refundBy" to responseBy,
                                        "remark" to responseRemark,
                                        "status" to "Rejected",
                                    )
                                )
                                orderViewModel.orderStatusUpdate(receipt.orderId, "REJECTED")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.error,
                                disabledContainerColor = AppColors.disabled
                            ),
                            enabled = isValid,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                "Reject",
                                color = AppColors.surface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(25.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RefundDetailPreview() {
    CanteenTheme {
        //RefundDetailPage(viewModel(), viewModel (), viewModel())
    }
}