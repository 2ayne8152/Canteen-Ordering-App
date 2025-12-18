package com.example.canteen.ui.screens.payment

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.canteen.data.RefundRequest
import com.example.canteen.ui.theme.CanteenTheme
import com.example.canteen.ui.theme.Green
import com.example.canteen.ui.theme.lightBlue
import com.example.canteen.data.Receipt
import com.example.canteen.ui.theme.veryLightRed
import com.example.canteen.viewmodel.payment.ReceiptViewModel
import com.example.menumanagement.BottomNavigationBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RefundManagementScreenWrapper(
    receiptViewModel: ReceiptViewModel,
    onBack: () -> Unit = {},
    onClick: () -> Unit = {},
    navController: NavController
) {
    val requested by receiptViewModel.requestedReceipts.collectAsState()
    val approved by receiptViewModel.approvedReceipts.collectAsState()
    val rejected by receiptViewModel.rejectedReceipts.collectAsState()

    RefundManagementScreen(
        receiptViewModel = receiptViewModel,
        pendingList = requested,
        approvedList = approved,
        rejectedList = rejected,
        onBack = onBack,
        onClick = onClick,
        navController = navController
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefundManagementScreen(
    receiptViewModel: ReceiptViewModel,
    pendingList: List<Pair<Receipt, RefundRequest?>>,
    approvedList: List<Pair<Receipt, RefundRequest?>>,
    rejectedList: List<Pair<Receipt, RefundRequest?>>,
    onBack: () -> Unit = {},
    onClick: () -> Unit,
    navController: NavController
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Requested", "Approved", "Rejected")
    var expandedCardId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Refund Management") },
                modifier = Modifier.shadow(6.dp)
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->

        Column(modifier = Modifier.padding(padding)) {
            Spacer(Modifier.height(4.dp))

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
                    .fillMaxSize().padding(start = 12.dp, top = 8.dp, end = 12.dp)
            ) {
                items(listToDisplay) { refundItem ->
                    when (selectedTab) {
                        0 -> RefundCard(
                            data = refundItem,
                            onClick = {
                                receiptViewModel.selectRefundItem(refundItem)
                                onClick()
                            }
                        )               // Pending
                        1 -> ApprovedRefundCard(
                            data = refundItem,
                            expanded = expandedCardId == refundItem.first.receiptId,
                            onClick = {
                                expandedCardId = if (expandedCardId == refundItem.first.receiptId) {
                                    null // collapse
                                } else {
                                    refundItem.first.receiptId // expand new card
                                }
                            }
                        )      // Approved
                        2 -> RejectedRefundCard(
                            data = refundItem,
                            expanded = expandedCardId == refundItem.first.receiptId,
                            onClick = {
                                expandedCardId = if (expandedCardId == refundItem.first.receiptId) {
                                    null // collapse
                                } else {
                                    refundItem.first.receiptId // expand new card
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
    data: Pair<Receipt, RefundRequest?>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Green,
        shadowElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp).clickable{onClick()}
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Order #${data.first.orderId.takeLast(6)}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                Text(text = formatTime(data.second?.requestTime ?: 0L) , fontSize = 14.sp, color = Color.Black)
            }

            Spacer(Modifier.height(4.dp))

            Text("Total : RM${String.format("%.2f", data.first.pay_Amount)}", color = Color.Black)
            Text("Refund Reason : ${data.second?.reason}", color = Color.Black)
        }
    }
}

@Composable
fun ApprovedRefundCard(
    data: Pair<Receipt, RefundRequest?>,
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
            .clickable(
                indication = if (expanded) LocalIndication.current else null,
                interactionSource = remember { MutableInteractionSource() }) { onClick() }
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Order #${data.first.orderId.takeLast(6)}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            }

            Spacer(Modifier.height(4.dp))

            Text("Total Refunded : RM${String.format("%.2f", data.first.pay_Amount)}", color = Color.Black)
            Text("Reason : ${data.second?.reason}", color = Color.Black)
            if (!expanded) {
                Text(
                    text = "Tap to view more",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            // ▼▼▼ ONLY SHOW WHEN EXPANDED ▼▼▼
            AnimatedVisibility(visible = expanded) {

                Column {

                    Spacer(Modifier.height(8.dp))
                    Divider()

                    Spacer(Modifier.height(8.dp))

                    Text("Admin: ${data.second?.refundBy}", color = Color.Black)

                    Spacer(Modifier.height(4.dp))

                    Text("Additional Notes:", color = Color.Black)
                    Text(
                        text = data.second?.remark ?: "",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun RejectedRefundCard(
    data: Pair<Receipt, RefundRequest?>,
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = veryLightRed,
        shadowElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(
                indication = if (expanded) LocalIndication.current else null,
                interactionSource = remember { MutableInteractionSource() }
            ) {onClick()}
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Order #${data.first.orderId.takeLast(6)}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            }

            Spacer(Modifier.height(4.dp))

            Text("Total Refunded : RM${String.format("%.2f", data.first.pay_Amount)}", color = Color.Black)
            Text("Reason : ${data.second?.reason}", color = Color.Black)
            if (!expanded) {
                Text(
                    text = "Tap to view more",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            AnimatedVisibility(visible = expanded) {

                Column {

                    Spacer(Modifier.height(8.dp))
                    Divider()

                    Spacer(Modifier.height(8.dp))

                    Text("Admin: ${data.second?.refundBy}", color = Color.Black)

                    Spacer(Modifier.height(4.dp))

                    Text("Reject Reason:", color = Color.Black)
                    Text(
                        text = data.second?.remark ?: "",
                        fontSize = 14.sp, color = Color.Black
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
        //RefundManagementScreenWrapper(receiptViewModel = viewModel())
    }
}