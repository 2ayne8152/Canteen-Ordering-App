package com.example.menumanagement

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.canteen.viewmodel.login.MenuViewModel
import com.example.canteen.ui.screens.CanteenScreen
import com.example.canteen.viewmodel.login.FirestoreMenuItem

@Composable
fun StaffDashboardScreen(
    navController: NavController,
    viewModel: MenuViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val menuItems by viewModel.menuItems.collectAsState()

    // Extract unique category IDs from menu items
    val categories = listOf("All") + menuItems.map { it.categoryId }.distinct()

    val filteredMenuItems = menuItems.filter { item ->
        val matchesCategory = selectedCategory == "All" || item.categoryId == selectedCategory
        val matchesSearch = item.name.contains(search.trim(), ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
        ) {

            Text("Menu Items", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Total Items: ${filteredMenuItems.size}", fontSize = 13.sp)

            Spacer(Modifier.height(12.dp))

            // Category chips
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    CategoryChip(
                        text = category,
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category }
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredMenuItems) { item ->
                    MenuItemCard(item)
                }
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

@Composable
fun MenuItemCard(item: FirestoreMenuItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.name,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, fontWeight = FontWeight.Bold)
            Text(
                item.description,
                fontSize = 13.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }

        Text(
            "RM %.2f".format(item.price),
            color = Color(0xFFFF6F3C),
            fontSize = 12.sp
        )
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {

        NavigationBarItem(
            selected = currentRoute == CanteenScreen.StaffDashboard.name,
            onClick = {
                navController.navigate(CanteenScreen.StaffDashboard.name) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = currentRoute == CanteenScreen.MenuItemForm.name,
            onClick = {
                navController.navigate(CanteenScreen.MenuItemForm.name) {
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Add, contentDescription = "Add Item") },
            label = { Text("Add Item") }
        )

        NavigationBarItem(
            selected = currentRoute == CanteenScreen.RefundManagementScreenWrapper.name,
            onClick = {
                navController.navigate(CanteenScreen.RefundManagementScreenWrapper.name) {
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.MonetizationOn, contentDescription = "Refund") },
            label = { Text("Refund") }
        )

        NavigationBarItem(
            selected = currentRoute == CanteenScreen.PaymentHistory.name,
            onClick = {
                navController.navigate(CanteenScreen.PaymentHistory.name) {
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.History, contentDescription = "Payment History") },
            label = { Text("History") }
        )

        NavigationBarItem(
            selected = false,
            onClick = {/* Report */},
            icon = { Icon(Icons.Default.Assessment, contentDescription = "Report") },
            label = { Text("Report") }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewUI() {
    val navController = rememberNavController()
    StaffDashboardScreen(navController)
}
