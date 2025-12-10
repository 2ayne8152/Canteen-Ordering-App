package com.example.canteen.ui.screens.payment

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.canteen.data.RefundRequest
import com.example.canteen.ui.theme.CanteenTheme
import com.example.canteen.ui.theme.Green
import com.example.canteen.ui.theme.lightBlue
import com.example.canteen.data.Receipt
import com.example.canteen.data.RefundItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*@Composable
fun RefundManagementScreenWrapper(
    viewModel: RefundViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    RefundManagementScreen(
        pendingList = viewModel.pendingList,
        approvedList = viewModel.approvedList,
        rejectedList = viewModel.rejectedList,
        onBack = onBack
    )
}*/


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefundManagementScreen(
    pendingList: List<RefundItem>,
    approvedList: List<RefundItem>,
    rejectedList: List<RefundItem>,
    onBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending", "Approved", "Rejected")
    var expandedCardId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Refund Management") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(modifier = Modifier.padding(padding)) {

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            val listToDisplay = when (selectedTab) {
                0 -> pendingList
                1 -> approvedList
                else -> rejectedList
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                items(listToDisplay) { refundItem ->
                    when (selectedTab) {
                        0 -> RefundCard(data = refundItem)               // Pending
                        1 -> ApprovedRefundCard(
                            data = refundItem,
                            expanded = expandedCardId == refundItem.receipt.orderId,
                            onClick = {
                                expandedCardId = if (expandedCardId == refundItem.receipt.orderId) {
                                    null // collapse
                                } else {
                                    refundItem.receipt.orderId // expand new card
                                }
                            }
                        )      // Approved
                        2 -> RejectedRefundCard(
                            data = refundItem,
                            expanded = expandedCardId == refundItem.receipt.orderId,
                            onClick = {
                                expandedCardId = if (expandedCardId == refundItem.receipt.orderId) {
                                    null // collapse
                                } else {
                                    refundItem.receipt.orderId // expand new card
                                }
                            }
                        )       // Rejected
                    }
                }
            }
        }
    }
}

@Composable
fun RefundCard(
    data: RefundItem,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Green,
        shadowElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp).clickable{}
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Order${data.receipt.orderId}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(text = formatTime(data.refund?.requestTime ?: 0L) , fontSize = 14.sp)
            }

            Spacer(Modifier.height(4.dp))

            Text("Total : RM${String.format("%.2f", data.receipt.pay_Amount)}")
            Text("Refund Reason : ${data.refund?.reason}")
        }
    }
}

@Composable
fun ApprovedRefundCard(
    data: RefundItem,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = lightBlue,
        shadowElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Order ${data.receipt.orderId}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(4.dp))

            Text("Total Refunded : RM${String.format("%.2f", data.receipt.pay_Amount)}")
            Text("Reason : ${data.refund.reason}")

            // ▼▼▼ ONLY SHOW WHEN EXPANDED ▼▼▼
            AnimatedVisibility(visible = expanded) {

                Column {

                    Spacer(Modifier.height(8.dp))
                    Divider()

                    Spacer(Modifier.height(8.dp))

                    Text("Admin: ${data.refund.refundBy}")

                    Spacer(Modifier.height(4.dp))

                    Text("Additional Notes:")
                    Text(
                        text = data.refund.remark,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RejectedRefundCard(
    data: RefundItem,
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Green,
        shadowElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable{onClick()}
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Order${data.receipt.orderId}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(4.dp))

            Text("Total : RM${String.format("%.2f", data.receipt.pay_Amount)}")
            Text("Refund Reason : ${data.refund?.reason}")

            AnimatedVisibility(visible = expanded) {

                Column {

                    Spacer(Modifier.height(8.dp))
                    Divider()

                    Spacer(Modifier.height(8.dp))

                    Text("Admin: ${data.refund.refundBy}")

                    Spacer(Modifier.height(4.dp))

                    Text("Reject Reason:")
                    Text(
                        text = data.refund.remark,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@SuppressLint("SimpleDateFormat")
fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// If Your Timestamp is in seconds (common in Firebase)
/*fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp * 1000)) // convert seconds → ms
}*/



@Preview(showBackground = true)
@Composable
fun RefundManagementPreview() {
    CanteenTheme {
        val sampleRefundList = listOf(
            RefundItem(
                receipt = Receipt(
                    orderId = "Order1234",
                    pay_Amount = 12.50),
                refund = RefundRequest(
                    reason = "Missing Item",
                    status = "pending",
                    requestTime = 1733985600L // 12/12/2025
                )
            ),
            RefundItem(
                receipt = Receipt(
                    orderId = "Order4567",
                    pay_Amount = 9.90),
                refund = RefundRequest(
                    reason = "Poor quality order",
                    status = "pending",
                    requestTime = 1733900000L)
            ),
            RefundItem(
                receipt = Receipt(
                    orderId = "Order9999",
                    pay_Amount = 5.00),
                refund = RefundRequest ( reason = "Change of Mind",
                status = "pending",
                requestTime = 1733800000L)
            )
        )

        val approvedRefundList = listOf(
            RefundItem(
                receipt = Receipt(
                    orderId = "Order1234",
                    pay_Amount = 12.50),
                refund = RefundRequest( reason = "Missing Item",
                status = "pending",
                requestTime = 1733985600L, // 12/12/2025
                refundBy = "Lili",
                remark = "Thank you")
            ),
            RefundItem(
                receipt = Receipt(
                    orderId = "Order4567",
                    pay_Amount = 9.90),
                refund = RefundRequest( reason = "Poor quality order",
                status = "pending",
                requestTime = 1733900000L)
            ),
            RefundItem(
                receipt = Receipt(
                    orderId = "Order9999",
                    pay_Amount = 5.00),
                refund = RefundRequest( reason = "Change of Mind",
                status = "pending",
                requestTime = 1733800000L)
            )
        )

        RefundManagementScreen(
            pendingList = sampleRefundList,
            approvedList = approvedRefundList,
            rejectedList = emptyList(),
            onBack = {}
        )
    }
}