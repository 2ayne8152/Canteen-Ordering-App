package com.example.canteen.ui.screens.payment

import android.view.Surface
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import com.example.canteen.ui.theme.AppColors

@Composable
fun PaymentBottomBar(
    modifier: Modifier = Modifier,
    itemCount: Int,
    totalAmount: Double,
    enabled: Boolean,
    onSubmit: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = AppColors.surface,
        shadowElevation = 12.dp,
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Summary row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Items",
                        fontSize = 12.sp,
                        color = AppColors.textSecondary
                    )
                    Text(
                        text = "$itemCount items",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.textPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Amount",
                        fontSize = 12.sp,
                        color = AppColors.textSecondary
                    )
                    Text(
                        text = "RM %.2f".format(totalAmount),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.primary
                    )
                }
            }

            // ✅ Submit button
            Button(
                onClick = onSubmit,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.primary,
                    disabledContainerColor = AppColors.disabled
                )
            ) {
                Text(
                    "Complete Payment",
                    color = AppColors.surface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}