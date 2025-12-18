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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.canteen.viewmodel.login.FirestoreMenuItem
import com.example.canteen.viewmodel.login.MenuViewModel
import com.example.canteen.ui.screens.CanteenScreen
import com.example.canteen.viewmodel.staffMenu.CategoryData
import com.example.canteen.ui.theme.AppColors


@Composable
fun StaffDashboardScreen(
    navController: NavController,
    onClick: () -> Unit,
    viewModel: MenuViewModel = viewModel()
){

    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val menuItems by viewModel.menuItems.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val filteredMenuItems = menuItems.filter { item ->
        (selectedCategory == "All" || item.categoryId == selectedCategory) &&
                item.name.contains(search.trim(), ignoreCase = true)
    }

    Scaffold(
        containerColor = AppColors.background,
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.background)
                .padding(16.dp)
        ) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Menu Items",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "Total Items: ${menuItems.size}",
                        fontSize = 14.sp,
                        color = AppColors.textSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                IconButton(onClick = { onClick() }) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = AppColors.textPrimary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Revenue Report",
                    subtitle = "View analytics",
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(CanteenScreen.ReportScreen.name) }
                )
                QuickActionCard(
                    title = "Orders Analytics",
                    subtitle = "Track orders",
                    icon = Icons.Default.Assessment,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(CanteenScreen.OrdersAnalyticsScreen.name) }
                )
            }

            Spacer(Modifier.height(20.dp))

            // Add & Edit buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { navController.navigate(CanteenScreen.MenuItemForm.name) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.primary
                    ),
                    shape = RoundedCornerShape(50.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        "+ Add New Item",
                        color = AppColors.surface,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 16.sp
                    )
                }

                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .clickable { navController.navigate(CanteenScreen.MenuListPage.name) },
                    color = AppColors.primary,
                    shadowElevation = 2.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Menu",
                            tint = AppColors.surface
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Search field
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Search menu items...", color = AppColors.textTertiary)
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = AppColors.textSecondary
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(50.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AppColors.surface,
                    unfocusedContainerColor = AppColors.surface,
                    focusedBorderColor = AppColors.divider,
                    unfocusedBorderColor = AppColors.divider,
                    cursorColor = AppColors.primary,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary
                )
            )

            Spacer(Modifier.height(16.dp))

            // Category Chips
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All" chip
                CategoryChip(
                    text = "All",
                    selected = selectedCategory == "All",
                    onClick = { selectedCategory = "All" }
                )

                // Other categories
                CategoryData.category.forEach { category ->
                    CategoryChip(
                        text = category.name,
                        selected = selectedCategory == category.name,
                        onClick = { selectedCategory = category.name }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Scrollable list of menu items
            LazyColumn(
                contentPadding = PaddingValues(
                    bottom = paddingValues.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredMenuItems) { item ->
                    MenuItemCard(
                        item = item,
                        onEditClick = {
                            navController.navigate(
                                "${CanteenScreen.StaffMenuDetailPage.name}/${item.id}"
                            )
                        }
                    )
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.primary
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
                tint = AppColors.surface,
                modifier = Modifier.size(28.dp)
            )

            Column {
                Text(
                    text = title,
                    color = AppColors.surface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = subtitle,
                    color = AppColors.surface.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


@Composable
fun CategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) AppColors.primary else AppColors.surface,
        shape = RoundedCornerShape(50.dp),
        modifier = Modifier.clickable { onClick() },
        shadowElevation = if (selected) 0.dp else 2.dp
    ) {
        Text(
            text,
            color = if (selected) AppColors.surface else AppColors.textPrimary,
            fontSize = 14.sp,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        )
    }
}

@Composable
fun MenuItemCard(
    item: FirestoreMenuItem,
    onEditClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val bitmap = remember(item.imageUrl) {
                item.imageUrl?.let { base64 ->
                    try {
                        val bytes = Base64.decode(base64, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            Box {
                bitmap?.let {
                    Image(
                        it.asImageBitmap(),
                        contentDescription = item.name,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } ?: Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.divider)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AppColors.textPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    item.description,
                    fontSize = 13.sp,
                    color = AppColors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
                Spacer(Modifier.height(8.dp))

                // Price tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AppColors.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        "RM %.2f".format(item.price),
                        color = AppColors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = AppColors.surface,
        contentColor = AppColors.textPrimary
    ) {
        NavigationBarItem(
            selected = currentRoute == CanteenScreen.StaffDashboard.name,
            onClick = { navController.navigate(CanteenScreen.StaffDashboard.name) { launchSingleTop = true } },
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Home",
                    tint = if (currentRoute == CanteenScreen.StaffDashboard.name)
                        AppColors.primary else AppColors.textSecondary
                )
            },
            label = {
                Text(
                    "Home",
                    color = if (currentRoute == CanteenScreen.StaffDashboard.name)
                        AppColors.primary else AppColors.textSecondary
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AppColors.primary,
                selectedTextColor = AppColors.primary,
                unselectedIconColor = AppColors.textSecondary,
                unselectedTextColor = AppColors.textSecondary,
                indicatorColor = AppColors.primary.copy(alpha = 0.15f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == CanteenScreen.MenuItemForm.name,
            onClick = { navController.navigate(CanteenScreen.StaffOrderStatusEdit.name) { launchSingleTop = true } },
            icon = {
                Icon(
                    Icons.Default.ReceiptLong,
                    contentDescription = "Edit Order",
                    tint = if (currentRoute == CanteenScreen.MenuItemForm.name)
                        AppColors.primary else AppColors.textSecondary
                )
            },
            label = {
                Text(
                    "Edit Order",
                    color = if (currentRoute == CanteenScreen.MenuItemForm.name)
                        AppColors.primary else AppColors.textSecondary
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AppColors.primary,
                selectedTextColor = AppColors.primary,
                unselectedIconColor = AppColors.textSecondary,
                unselectedTextColor = AppColors.textSecondary,
                indicatorColor = AppColors.primary.copy(alpha = 0.15f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == CanteenScreen.RefundManagementScreenWrapper.name,
            onClick = { navController.navigate(CanteenScreen.RefundManagementScreenWrapper.name) { launchSingleTop = true } },
            icon = {
                Icon(
                    Icons.Default.MonetizationOn,
                    contentDescription = "Refund",
                    tint = if (currentRoute == CanteenScreen.RefundManagementScreenWrapper.name)
                        AppColors.primary else AppColors.textSecondary
                )
            },
            label = {
                Text(
                    "Refund",
                    color = if (currentRoute == CanteenScreen.RefundManagementScreenWrapper.name)
                        AppColors.primary else AppColors.textSecondary
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AppColors.primary,
                selectedTextColor = AppColors.primary,
                unselectedIconColor = AppColors.textSecondary,
                unselectedTextColor = AppColors.textSecondary,
                indicatorColor = AppColors.primary.copy(alpha = 0.15f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == CanteenScreen.PaymentHistory.name,
            onClick = { navController.navigate(CanteenScreen.PaymentHistory.name) { launchSingleTop = true } },
            icon = {
                Icon(
                    Icons.Default.History,
                    contentDescription = "Payment History",
                    tint = if (currentRoute == CanteenScreen.PaymentHistory.name)
                        AppColors.primary else AppColors.textSecondary
                )
            },
            label = {
                Text(
                    "History",
                    color = if (currentRoute == CanteenScreen.PaymentHistory.name)
                        AppColors.primary else AppColors.textSecondary
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AppColors.primary,
                selectedTextColor = AppColors.primary,
                unselectedIconColor = AppColors.textSecondary,
                unselectedTextColor = AppColors.textSecondary,
                indicatorColor = AppColors.primary.copy(alpha = 0.15f)
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(CanteenScreen.ReportScreen.name) },
            icon = {
                Icon(
                    Icons.Default.Assessment,
                    contentDescription = "Report",
                    tint = AppColors.textSecondary
                )
            },
            label = {
                Text(
                    "Report",
                    color = AppColors.textSecondary
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AppColors.primary,
                selectedTextColor = AppColors.primary,
                unselectedIconColor = AppColors.textSecondary,
                unselectedTextColor = AppColors.textSecondary,
                indicatorColor = AppColors.primary.copy(alpha = 0.15f)
            )
        )
    }
}