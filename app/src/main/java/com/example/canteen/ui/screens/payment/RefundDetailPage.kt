package com.example.canteen.ui.screens.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.canteen.ui.theme.CanteenTheme
import com.example.canteen.ui.theme.lightBlue
import com.example.canteen.ui.theme.lightGreen
import com.example.canteen.ui.theme.lightRed
import com.example.canteen.ui.theme.lightViolet
import com.example.canteen.ui.theme.softGreen
import com.example.canteen.ui.theme.veryLightRed


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefundDetailPage(onBack: () -> Unit = {}) {
    var responseBy by remember { mutableStateOf("") }
    var responseRemark by remember { mutableStateOf("") }

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
                        Text("Order1234", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("12:00 12/4/2025")
                    }

                    Text("Student ID : student13")
                    Spacer(Modifier.height(4.dp))
                    Text("Total : RM16.00", fontWeight = FontWeight.Bold)
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

                    Text("Order Items :", fontWeight = FontWeight.Bold, fontSize = 20.sp)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Set A x1")
                        Text("RM 15.00")
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Add On Rice x1")
                        Text("RM 1.00")
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

                    Text("Refund Request :", fontWeight = FontWeight.Bold, fontSize = 20.sp)

                    Text("Reason : Missing Item", fontWeight = FontWeight.SemiBold)

                    Spacer(Modifier.height(4.dp))
                    Text("Detail :", fontWeight = FontWeight.SemiBold)
                    Text(
                        "When receiving my order, I found out that my add-on rice is missing. ......",
                        lineHeight = 20.sp
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

                    Text("Refund Response :", fontWeight = FontWeight.Bold)

                    Spacer(Modifier.height(4.dp))

                    OutlinedTextField(
                        value = responseBy,
                        onValueChange = { responseBy = it },
                        label = { Text("Response By") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = responseRemark,
                        onValueChange = { responseRemark = it },
                        label = { Text("Remark") },
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
                            onClick = { /* Approve logic */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = lightGreen
                            ),
                            elevation = ButtonDefaults.buttonElevation(8.dp)
                        ) {
                            /*Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(Modifier.width(6.dp))*/
                            Text("Approve Refund")
                        }

                        Button(
                            onClick = { /* Reject logic */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = lightRed// red
                            ),
                            elevation = ButtonDefaults.buttonElevation(8.dp)
                        ) {
                            /*Icon(Icons.Default.Close, contentDescription = null)
                            Spacer(Modifier.width(6.dp))*/
                            Text("Reject Refund")
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
        RefundDetailPage()
    }
}