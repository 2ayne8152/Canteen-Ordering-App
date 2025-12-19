package com.example.canteen.ui.screens.payment

import android.R.attr.text
import android.os.Build
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.canteen.ui.theme.AppColors
import com.example.canteen.ui.theme.CanteenTheme
import com.example.canteen.ui.theme.black
import com.example.canteen.ui.theme.blue
import com.example.canteen.ui.theme.veryLightBlue
import com.example.canteen.ui.theme.white
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.canteen.ui.theme.gray
import com.example.canteen.ui.theme.lightBlue
import com.example.canteen.ui.theme.lightRed
import com.example.canteen.viewmodel.payment.CardDetailViewModel
import com.example.canteen.viewmodel.payment.PaymentMethodViewModel
import kotlin.math.sin
import androidx.compose.ui.graphics.Color

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PaymentMethod(
    paymentMethodViewModel: PaymentMethodViewModel = viewModel(),
    phoneNumber: String,
    onMethodSelected: (String?) -> Unit,
    onCardValidityChange: (Boolean) -> Unit
) {
    val selectedMethod = paymentMethodViewModel.selectedMethod.value

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.background)
    ) {
        Text(
            text = "Select Payment Method",
            style = MaterialTheme.typography.titleLarge,
            fontSize = 24.sp,
            color = AppColors.textPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        // ------------------- CREDIT / DEBIT CARD -------------------
        PaymentOptionCard(
            title = "Credit/Debit Card",
            icon = Icons.Outlined.CreditCard,
            selected = selectedMethod == "Card",
            onClick = {
                val newMethod = if (selectedMethod == "Card") null else "Card"
                paymentMethodViewModel.select(newMethod)
                onMethodSelected(newMethod)
            }
        )

        Spacer(Modifier.height(12.dp))

        // ------------------- E-WALLET -------------------
        PaymentOptionCard(
            title = "E-Wallet",
            icon = Icons.Outlined.Wallet,
            selected = selectedMethod == "E-wallet",
            onClick = {
                val newMethod = if (selectedMethod == "E-wallet") null else "E-wallet"
                paymentMethodViewModel.select(newMethod)
                onMethodSelected(newMethod)
            }
        )

        AnimatedVisibility(visible = selectedMethod == "Card") {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                PayByCard(
                    onValidityChange = onCardValidityChange
                )
            }
        }

        // Expand section for E-Wallet
        AnimatedVisibility(visible = selectedMethod == "E-wallet") {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                val formattedNumber = "${phoneNumber.substring(0, 3)}-${phoneNumber.substring(3, 6)} ${phoneNumber.substring(6)}"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "E-Wallet Phone Number",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 18.sp,
                            color = AppColors.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(16.dp))

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = AppColors.primary.copy(alpha = 0.15f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = AppColors.success,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = formattedNumber,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.textPrimary
                                    )
                                    Text(
                                        text = "Linked account",
                                        fontSize = 13.sp,
                                        color = AppColors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentOptionCard(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) AppColors.primary else AppColors.divider
    val backgroundColor = if (selected) AppColors.primary.copy(alpha = 0.1f) else AppColors.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = { onClick() },
                colors = RadioButtonDefaults.colors(
                    selectedColor = AppColors.primary,
                    unselectedColor = AppColors.textSecondary
                )
            )

            Spacer(Modifier.width(12.dp))

            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) AppColors.primary else AppColors.textSecondary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(Modifier.width(12.dp))

            Text(
                title,
                fontSize = 17.sp,
                color = AppColors.textPrimary,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PaymentOptionPreview() {
    CanteenTheme {
        //PaymentMethod(savedCard = "Visa ending 4321", phoneNumber = "0123456789", cardDetailViewModel = viewModel())
    }
}