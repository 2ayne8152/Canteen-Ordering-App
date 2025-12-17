package com.example.menumanagement

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.canteen.viewmodel.login.Category
import com.example.canteen.viewmodel.login.FirestoreMenuItem
import com.example.canteen.viewmodel.login.MenuViewModel
import com.example.canteen.ui.screens.CanteenScreen
import kotlinx.coroutines.launch

@Composable
fun StaffDashboardScreen(navController: NavController, viewModel: MenuViewModel = viewModel(), onClick: () -> Unit) {

    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val menuItems by viewModel.menuItems.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val filteredMenuItems = menuItems.filter { item ->
        (selectedCategory == "All" || item.categoryId == selectedCategory) &&
                item.name.contains(search.trim(), ignoreCase = true)
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F7F7))
                .padding(16.dp)
        ) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Staff Dashboard", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Menu Management System", fontSize = 13.sp, color = Color.Gray)
                }
                Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.Blue, modifier = Modifier.clickable(onClick = {onClick()}))
            }

            Spacer(Modifier.height(20.dp))

            // Quick Action Cards for Reports
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Sales Report",
                    subtitle = "View analytics",
                    icon = Icons.Default.TrendingUp,
                    backgroundColor = Color(0xFF0A3D91),
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(CanteenScreen.ReportScreen.name) }
                )
                QuickActionCard(
                    title = "Orders Analytics",
                    subtitle = "Track orders",
                    icon = Icons.Default.Assessment,
                    backgroundColor = Color(0xFF1976D2),
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(CanteenScreen.OrdersAnalyticsScreen.name) }
                )
            }

            Spacer(Modifier.height(20.dp))

            Text("Menu Items Management", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Total Items: ${menuItems.size}", fontSize = 13.sp, color = Color.Gray)
            Spacer(Modifier.height(16.dp))

            // Add & Edit buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { navController.navigate(CanteenScreen.MenuItemForm.name) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A3D91)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ Add New Item", color = Color.White)
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0D47A1))
                        .clickable { navController.navigate(CanteenScreen.MenuListPage.name) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Menu",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Search field
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search menu items...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Blue) },
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Category Chips
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val allCategories = listOf(Category("", "All", "")) + categories
                allCategories.forEach { category ->
                    CategoryChip(
                        text = category.Name,
                        selected = selectedCategory == category.CategoryID || (category.Name == "All" && selectedCategory == "All"),
                        onClick = { selectedCategory = category.CategoryID.ifBlank { "All" } }
                    )
                }
            }

            // Scrollable list of menu items
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                items(filteredMenuItems) { item ->
                    MenuItemCard(item)
                }
            }
        }
    }
}

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


@Composable
fun CategoryChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Color(0xFF0A3D91) else Color(0xFFEFEFEF))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text, color = if (selected) Color.White else Color.Black, fontSize = 13.sp)
    }
}

@Composable
fun MenuItemCard(item: FirestoreMenuItem, onEditClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bitmap = remember(item.imageUrl) {
            item.imageUrl?.let { base64 ->
                try {
                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } catch (e: Exception) { null }
            }
        }

        bitmap?.let {
            Image(
                it.asImageBitmap(),
                contentDescription = item.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        } ?: Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray)
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(item.description, fontSize = 13.sp, color = Color.Gray, maxLines = 1)
        }

        // Price tag
        Box(
            modifier = Modifier
                .background(Color(0xFFFFE0C2), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text("RM %.2f".format(item.price), color = Color(0xFFFF6F3C), fontSize = 12.sp)
        }

        Spacer(Modifier.width(8.dp))

    }
}


@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == CanteenScreen.StaffDashboard.name,
            onClick = { navController.navigate(CanteenScreen.StaffDashboard.name) { launchSingleTop = true } },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentRoute == CanteenScreen.MenuItemForm.name,
            onClick = { navController.navigate(CanteenScreen.MenuItemForm.name) { launchSingleTop = true } },
            icon = { Icon(Icons.Default.Add, contentDescription = "AddItem") },
            label = { Text("AddItem") }
        )
        NavigationBarItem(
            selected = currentRoute == CanteenScreen.RefundManagementScreenWrapper.name,
            onClick = { navController.navigate(CanteenScreen.RefundManagementScreenWrapper.name) { launchSingleTop = true } },
            icon = { Icon(Icons.Default.MonetizationOn, contentDescription = "Refund") },
            label = { Text("Refund") }
        )
        NavigationBarItem(
            selected = currentRoute == CanteenScreen.PaymentHistory.name,
            onClick = { navController.navigate(CanteenScreen.PaymentHistory.name) { launchSingleTop = true } },
            icon = { Icon(Icons.Default.History, contentDescription = "Payment History") },
            label = { Text("History") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* Report */ },
            icon = { Icon(Icons.Default.Assessment, contentDescription = "Report") },
            label = { Text("Report") }
        )
    }
}
