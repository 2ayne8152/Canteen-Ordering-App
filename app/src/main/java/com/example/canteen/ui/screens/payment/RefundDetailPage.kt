package com.example.canteen.ui.screens.payment

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
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
import com.example.canteen.viewmodel.usermenu.order.OrderViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefundDetailPage(
    orderViewModel: OrderViewModel,
    receiptViewModel: ReceiptViewModel,
    refundViewModel: RefundViewModel,
    userViewModel: UserViewModel,
    onBack: () -> Unit = {}
) {
    val order by orderViewModel.latestOrder.collectAsState()
    val user by userViewModel.selectedUser.collectAsState()
    val selected by receiptViewModel.selectedRefund.collectAsState()
    var responseBy by remember { mutableStateOf("") }
    var responseRemark by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var textFieldWidth by remember { mutableStateOf(0.dp) }
    val isValid = responseBy.isNotBlank() && responseRemark.isNotBlank()

    if (selected == null) {
        Text("Loading...", color = Color.Black)
        return
    }

    val receipt = selected!!.first
    val refund = selected!!.second

    LaunchedEffect(receipt.orderId) {
        orderViewModel.getOrder(receipt.orderId)
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 6.dp) {
                TopAppBar(
                    title = { Text("Refund Detail") },
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
                .verticalScroll(rememberScrollState())
        ) {

            // ------------------------ ORDER INFO CARD ------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = softGreen
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(Modifier.padding(16.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Order ${receipt.orderId.take(6)}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                        Text(text = formatTime(refund?.requestTime ?: 0L), color = Color.Black)
                    }

                    Spacer(Modifier.height(4.dp))
                    Text("Total : RM${"%.2f".format(receipt.pay_Amount)}", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ------------------------ ORDER ITEMS CARD ------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = lightBlue
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(Modifier.padding(16.dp)) {

                    Text("Order Items :", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)

                    order?.items?.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.menuItem.name} x${item.quantity}",
                                color = Color.Black
                            )
                            Text(
                                text = "RM ${"%.2f".format(item.totalPrice)}",
                                color = Color.Black
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
                    containerColor = lightViolet
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(Modifier.padding(16.dp)) {

                    Text("Refund Request :", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)

                    Text("Reason : ${refund?.reason}", fontWeight = FontWeight.SemiBold, color = Color.Black)

                    Spacer(Modifier.height(4.dp))
                    Text("Detail :", fontWeight = FontWeight.SemiBold, color = Color.Black)
                    Text(
                        refund?.refundDetail ?: "",
                        lineHeight = 20.sp, color = Color.Black
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ------------------------ REFUND RESPONSE CARD ------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = veryLightRed
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(Modifier.padding(16.dp)) {

                    Text("Refund Response :", fontWeight = FontWeight.Bold, color = Color.Black)

                    Spacer(Modifier.height(4.dp))

                    /*OutlinedTextField(
                        value = responseBy,
                        onValueChange = { responseBy = it },
                        label = { Text("Response By", color = Color.Black) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )*/

                    Box {
                        val density = LocalDensity.current
                        OutlinedTextField(
                            value = responseBy,
                            onValueChange = {
                                responseBy = it
                                            },
                            label = { Text("Response By", color = Color.Black) },
                            singleLine = true,
                            trailingIcon = {
                                Icon(
                                    if (expanded) Icons.Default.KeyboardArrowUp
                                    else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
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
                            keyboardActions = KeyboardActions(onDone = {expanded = false})
                        )

                        DropdownMenu(
                            modifier = Modifier.width(textFieldWidth),
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = user?.Name.orEmpty(), color = Color.Black) },
                                onClick = {
                                    responseBy = user?.Name.orEmpty()
                                    expanded = false
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = responseRemark,
                        onValueChange = { responseRemark = it },
                        label = { Text("Remark", color = Color.Black) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5
                    )

                    Spacer(Modifier.height(16.dp))

                    // Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
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
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = lightGreen
                            ),
                            elevation = ButtonDefaults.buttonElevation(8.dp),
                            enabled = isValid
                        ) {
                            /*Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(Modifier.width(6.dp))*/
                            Text("Approve Refund", color = Color.White)
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
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = lightRed// red
                            ),
                            elevation = ButtonDefaults.buttonElevation(8.dp),
                            enabled = isValid
                        ) {
                            /*Icon(Icons.Default.Close, contentDescription = null)
                            Spacer(Modifier.width(6.dp))*/
                            Text("Reject Refund", color = Color.White)
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