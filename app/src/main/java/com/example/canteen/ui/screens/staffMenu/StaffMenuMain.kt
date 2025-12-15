package com.example.menumanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Import your actual menu data model
import com.example.canteen.data.MenuItem
import com.example.canteen.data.menuItems

// Example category list (you can change it)
val categories = listOf("All", "Chicken Rice", "Curry Mee", "Tomyam Maggi")

// ===========================================================
// FULL DASHBOARD UI
// ===========================================================
@Composable
fun StaffDashboardScreen(
    onNavigateToReports: () -> Unit = {},
    onNavigateToOrdersAnalytics: () -> Unit = {}
) {
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .padding(16.dp)
    ) {

        // ---------- Top Header ----------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Staff Dashboard", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Menu Management System", fontSize = 13.sp, color = Color.Gray)
            }
            Icon(
                Icons.Default.Logout,
                contentDescription = "Logout",
                tint = Color.Blue
            )
        }

        Spacer(Modifier.height(20.dp))

        // ---------- Quick Actions Section ----------
        Text("Quick Actions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "Revenue Report",
                subtitle = "View analytics",
                icon = Icons.Default.TrendingUp,
                backgroundColor = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToReports() }
            )

            QuickActionCard(
                title = "Orders",
                subtitle = "Manage orders",
                icon = Icons.Default.ShoppingCart,
                backgroundColor = Color(0xFF2196F3),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToOrdersAnalytics() }  // Changed from empty comment to actual navigation
            )
        }

        Spacer(Modifier.height(20.dp))

        // Section Title
        Text("Menu Items Management", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Total Items: ${menuItems.size}", fontSize = 13.sp, color = Color.Gray)
        Spacer(Modifier.height(16.dp))

        // Add New Item Button
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0A3D91)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("+ Add New Item", fontSize = 15.sp, color = Color.White)
        }

        Spacer(Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search menu items...") },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Blue
                )
            },
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(Modifier.height(16.dp))

        // Category Chips
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            categories.forEach { category ->
                CategoryChip(
                    text = category,
                    selected = category == selectedCategory,
                    onClick = { selectedCategory = category }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ===========================================================
        // MENU LIST (real data from your data class)
        // ===========================================================
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(menuItems) { item ->
                MenuItemCard(item)
            }
        }
    }
}

// ===========================================================
// QUICK ACTION CARD
// ===========================================================
@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )

            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ===========================================================
// CATEGORY CHIP
// ===========================================================
@Composable
fun CategoryChip(text: String, selected: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) Color(0xFF0A3D91) else Color(0xFFEFEFEF)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Text(
            text,
            color = if (selected) Color.White else Color.Black,
            fontSize = 13.sp
        )
    }
}

// ===========================================================
// MENU ITEM CARD (uses your real MenuItem)
// ===========================================================
@Composable
fun MenuItemCard(item: MenuItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Actual Image
        Image(
            painter = painterResource(id = item.imageRes),
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(id = item.itemName),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                text = stringResource(id = item.itemDesc),
                fontSize = 13.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }

        // Price Tag
        Box(
            modifier = Modifier
                .background(Color(0xFFFFE0C2), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "RM %.2f".format(item.itemPrice),
                color = Color(0xFFFF6F3C),
                fontSize = 12.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewUI() {
    StaffDashboardScreen()
}