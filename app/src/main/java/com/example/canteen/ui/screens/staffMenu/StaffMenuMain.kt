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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

// Your menu data
import com.example.canteen.data.MenuItem
import com.example.canteen.data.menuItems
import com.example.canteen.ui.screens.CanteenScreen

// Category List
val categories = listOf("All", "Chicken Rice", "Curry Mee", "Tomyam Maggi")

// ===================================================================
// MAIN UI WITH BOTTOM BAR
// ===================================================================
@Composable
fun StaffDashboardScreen(navController: NavController)  {
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F7F7))
                .padding(16.dp)
                .padding(paddingValues)
        ) {

            // -------------------- Header --------------------
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

            // -------------------- Section Title --------------------
            Text("Menu Items", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Total Items: ${menuItems.size}", fontSize = 13.sp, color = Color.Gray)
            Spacer(Modifier.height(16.dp))

            // -------------------- Add New + Edit --------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A3D91)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ Add New Item", fontSize = 15.sp, color = Color.White)
                }

                Spacer(Modifier.width(12.dp))

                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Menu",
                        tint = Color.Blue
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // -------------------- Search Bar --------------------
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search menu items...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Blue)
                },
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.height(16.dp))

            // -------------------- Category Chips --------------------
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

            // -------------------- Menu List --------------------
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
}

// ===================================================================
// CATEGORY CHIP
// ===================================================================
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

// ===================================================================
// MENU ITEM CARD
// ===================================================================
@Composable
fun MenuItemCard(item: MenuItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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

        Box(
            modifier = Modifier
                .background(Color(0xFFFFE0C2), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                "RM %.2f".format(item.itemPrice),
                color = Color(0xFFFF6F3C),
                fontSize = 12.sp
            )
        }
    }
}

// ===================================================================
// BOTTOM NAVIGATION BAR
// ===================================================================
@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(CanteenScreen.StaffDashboard.name) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { androidx.compose.material3.Text("Home") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(CanteenScreen.MenuItemForm.name) },
            icon = { Icon(Icons.Default.Add, contentDescription = "Add Item") },
            label = { androidx.compose.material3.Text("Add Item") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(CanteenScreen.StaffDashboard.name) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { androidx.compose.material3.Text("Profile") }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewUI() {
    val navController = rememberNavController()
    StaffDashboardScreen(navController)
}
