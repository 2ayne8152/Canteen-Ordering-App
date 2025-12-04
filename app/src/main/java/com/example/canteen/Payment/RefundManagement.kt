package com.example.canteen.Payment

import android.annotation.SuppressLint
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.canteen.ui.theme.CanteenTheme
import com.example.canteen.ui.theme.Green

@Composable
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
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefundManagementScreen(
    pendingList: List<RefundRequest>,
    approvedList: List<RefundRequest>,
    rejectedList: List<RefundRequest>,
    onBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending", "Approved", "Rejected")

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
                    RefundCard(data = refundItem)
                }
            }
        }
    }
}

@Composable
fun RefundCard(
    data: RefundRequest,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Green,
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Order${data.orderId}", fontSize = 16.sp)
                Text(text = formatTime(data.requestTime) , fontSize = 14.sp)
            }

            Spacer(Modifier.height(4.dp))

            Text("Student ID : ${data.studentId}")
            Text("Total : RM${String.format("%.2f", data.total)}")
            Text("Refund Reason : ${data.reason}")
        }
    }
}

@SuppressLint("SimpleDateFormat")
fun formatTime(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("HH:mm dd/MM/yyyy")
    return sdf.format(java.util.Date(timestamp))
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
            RefundRequest(
                orderId = "Order1234",
                studentId = "student13",
                total = 12.50,
                reason = "Missing Item",
                status = "pending",
                requestTime = 1733985600L // 12/12/2025
            ),
            RefundRequest(
                orderId = "Order4567",
                studentId = "student22",
                total = 9.90,
                reason = "Poor quality order",
                status = "pending",
                requestTime = 1733900000L
            ),
            RefundRequest(
                orderId = "Order9999",
                studentId = "student31",
                total = 5.00,
                reason = "Change of Mind",
                status = "pending",
                requestTime = 1733800000L
            )
        )

        RefundManagementScreen(
            pendingList = sampleRefundList,
            approvedList = emptyList(),
            rejectedList = emptyList(),
            onBack = {}
        )
    }
}