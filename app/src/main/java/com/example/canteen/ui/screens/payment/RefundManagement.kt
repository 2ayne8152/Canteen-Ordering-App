package com.example.canteen.ui.screens.payment

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.Alignment
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
import com.example.canteen.ui.theme.AppColors
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
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Refund Management",
                        color = AppColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.surface,
                    titleContentColor = AppColors.textPrimary
                ),
                modifier = Modifier.shadow(4.dp)
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(AppColors.background)
        ) {
            // Custom TabRow with dark theme
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = AppColors.surface,
                contentColor = AppColors.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = AppColors.primary,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) AppColors.primary else AppColors.textSecondary
                            )
                        }
                    )
                }
            }

            val listToDisplay = when (selectedTab) {
                0 -> pendingList
                1 -> approvedList
                else -> rejectedList
            }

            if (listToDisplay.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = AppColors.textTertiary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "No ${tabs[selectedTab].lowercase()} refunds",
                            color = AppColors.textSecondary,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listToDisplay) { refundItem ->
                        when (selectedTab) {
                            0 -> RefundCard(
                                data = refundItem,
                                onClick = {
                                    receiptViewModel.selectRefundItem(refundItem)
                                    onClick()
                                }
                            )
                            1 -> ApprovedRefundCard(
                                data = refundItem,
                                expanded = expandedCardId == refundItem.first.receiptId,
                                onClick = {
                                    expandedCardId = if (expandedCardId == refundItem.first.receiptId) {
                                        null
                                    } else {
                                        refundItem.first.receiptId
                                    }
                                }
                            )
                            2 -> RejectedRefundCard(
                                data = refundItem,
                                expanded = expandedCardId == refundItem.first.receiptId,
                                onClick = {
                                    expandedCardId = if (expandedCardId == refundItem.first.receiptId) {
                                        null
                                    } else {
                                        refundItem.first.receiptId
                                    }
                                }
                            )
                        }
                    }
                    // Add spacing at the bottom
                    item { Spacer(Modifier.height(8.dp)) }
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
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Order #${data.first.orderId.takeLast(6)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AppColors.warning.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "PENDING",
                        color = AppColors.warning,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Time
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = AppColors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = formatTime(data.second?.requestTime ?: 0L),
                    fontSize = 14.sp,
                    color = AppColors.textSecondary
                )
            }

            Spacer(Modifier.height(8.dp))

            Divider(color = AppColors.divider)

            Spacer(Modifier.height(12.dp))

            // Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Refund Amount:",
                    color = AppColors.textSecondary,
                    fontSize = 14.sp
                )
                Text(
                    "RM ${String.format("%.2f", data.first.pay_Amount)}",
                    color = AppColors.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            // Reason
            Text(
                "Reason:",
                color = AppColors.textSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                data.second?.reason ?: "",
                color = AppColors.textPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(12.dp))

            // Action hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Tap to review",
                    color = AppColors.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = AppColors.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
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
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Order #${data.first.orderId.takeLast(6)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AppColors.success.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "APPROVED",
                        color = AppColors.success,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Refunded Amount:",
                    color = AppColors.textSecondary,
                    fontSize = 14.sp
                )
                Text(
                    "RM ${String.format("%.2f", data.first.pay_Amount)}",
                    color = AppColors.success,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            // Reason
            Text(
                "Reason:",
                color = AppColors.textSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                data.second?.reason ?: "",
                color = AppColors.textPrimary,
                fontSize = 14.sp
            )

            if (!expanded) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tap to view details",
                        fontSize = 13.sp,
                        color = AppColors.textTertiary
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = AppColors.textTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    Divider(color = AppColors.divider)
                    Spacer(Modifier.height(12.dp))

                    // Admin info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Approved By:",
                            color = AppColors.textSecondary,
                            fontSize = 14.sp
                        )
                        Text(
                            data.second?.refundBy ?: "",
                            color = AppColors.textPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Remark
                    Text(
                        "Additional Notes:",
                        color = AppColors.textSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = data.second?.remark ?: "No additional notes",
                        fontSize = 14.sp,
                        color = AppColors.textPrimary,
                        lineHeight = 20.sp
                    )

                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Tap to collapse",
                            fontSize = 13.sp,
                            color = AppColors.textTertiary
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = null,
                            tint = AppColors.textTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
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
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Order #${data.first.orderId.takeLast(6)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AppColors.error.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "REJECTED",
                        color = AppColors.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Requested Amount:",
                    color = AppColors.textSecondary,
                    fontSize = 14.sp
                )
                Text(
                    "RM ${String.format("%.2f", data.first.pay_Amount)}",
                    color = AppColors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            // Reason
            Text(
                "Reason:",
                color = AppColors.textSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                data.second?.reason ?: "",
                color = AppColors.textPrimary,
                fontSize = 14.sp
            )

            if (!expanded) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tap to view details",
                        fontSize = 13.sp,
                        color = AppColors.textTertiary
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = AppColors.textTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    Divider(color = AppColors.divider)
                    Spacer(Modifier.height(12.dp))

                    // Admin info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Rejected By:",
                            color = AppColors.textSecondary,
                            fontSize = 14.sp
                        )
                        Text(
                            data.second?.refundBy ?: "",
                            color = AppColors.textPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Reject Reason
                    Text(
                        "Rejection Reason:",
                        color = AppColors.textSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = data.second?.remark ?: "No reason provided",
                        fontSize = 14.sp,
                        color = AppColors.textPrimary,
                        lineHeight = 20.sp
                    )

                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Tap to collapse",
                            fontSize = 13.sp,
                            color = AppColors.textTertiary
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = null,
                            tint = AppColors.textTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
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