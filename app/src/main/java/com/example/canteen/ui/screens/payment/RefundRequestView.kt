package com.example.canteen.ui.screens.payment

import android.util.Log
import android.view.Surface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.canteen.ui.theme.CanteenTheme
import com.example.canteen.ui.theme.darkGray
import com.example.canteen.ui.theme.isVeryLightBlue
import com.example.canteen.ui.theme.middleGray
import com.example.canteen.ui.theme.veryLightBlue
import com.example.canteen.viewmodel.payment.ReceiptViewModel

enum class RefundStatus {
    APPROVED,
    REQUESTED,
    REJECTED;

    companion object {
        fun from(value: String?): RefundStatus {
            return when (value?.lowercase()) {
                "approved" -> APPROVED
                "rejected" -> REJECTED
                "requested" -> REQUESTED
                else -> REQUESTED
            }
        }
    }
}


@Composable
fun RefundStatusChip(status: RefundStatus) {
    val (bgColor, textColor, text) = when (status) {
        RefundStatus.APPROVED -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Approved")
        RefundStatus.REQUESTED -> Triple(Color(0xFFFFF8E1), Color(0xFFF9A825), "Requested")
        RefundStatus.REJECTED -> Triple(Color(0xFFFDECEA), Color(0xFFC62828), "Rejected")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun RefundRequestCard(
    reason: String,
    requestTime: String,
    status: RefundStatus,
    remark: String? = null
) {
    Column(modifier = Modifier
        .animateContentSize()) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Refund Request",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold, color = Color.Black
            )

            RefundStatusChip(status = status)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Reason: $reason",
            fontSize = 14.sp,
            color = darkGray
        )

        Text(
            text = "Request At : $requestTime",
            fontSize = 14.sp,
            color = darkGray
        )

        if (status.name != "REQUESTED"){
            Column(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
            ) {

                Divider(modifier = Modifier.padding(bottom = 8.dp))

                Text(
                    text = "Admin Remark : ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                Text(
                    text = remark.orEmpty(),
                    fontSize = 13.sp,
                    color = darkGray
                )
            }
        }

    }
}


@Composable
fun RefundDetailScreen(
    orderId: String,
    receiptViewModel: ReceiptViewModel
) {
    /*LaunchedEffect(orderId) {
        receiptViewModel.loadReceiptByOrderId(orderId)
    }*/

    val receiptPair by receiptViewModel.receiptLoadByOrderId.collectAsState()
    Log.w("Refund", receiptPair?.first?.receiptId ?: "")

    when {
        receiptPair == null -> {
            CircularProgressIndicator()
        }

        else -> {
            val (receipt, refund) = receiptPair!!
                RefundRequestCard(
                    reason = refund?.reason ?: "No reason provided",
                    requestTime = refund?.requestTime?.let { formatTime(it) } ?: "-",
                    status = RefundStatus.from(refund?.status),
                    remark = refund?.remark
                )
        }
    }
}



@Composable
fun RefundCardPreview() {
    RefundDetailScreen(
        orderId = "874cc51d-bca3-4248-a6fd-46c6f33fbea2", receiptViewModel = viewModel())
}

@Preview(showBackground = true)
@Composable
fun PaymentCardPreview() {
    CanteenTheme {
        RefundCardPreview()
    }
}

