package com.example.menumanagement

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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


@OptIn(ExperimentalMaterial3Api::class)
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
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Menu Items",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.textPrimary
                        )
                        Text(
                            "Total Items: ${menuItems.size}",
                            fontSize = 13.sp,
                            color = AppColors.textSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onClick) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = AppColors.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.surface,
                    titleContentColor = AppColors.textPrimary
                ),
                modifier = Modifier.shadow(4.dp)
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.background)
                .padding(start = 12.dp, end = 12.dp, top = 14.dp).padding(paddingValues)
        ) {

            // Quick Action Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Revenue Report",
                    subtitle = "View analytics",
                    icon = Icons.Default.TrendingUp,
                    backgroundColor = AppColors.primary,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(CanteenScreen.ReportScreen.name) }
                )
                QuickActionCard(
                    title = "Orders Analytics",
                    subtitle = "Track orders",
                    icon = Icons.Default.Assessment,
                    backgroundColor = AppColors.primary,
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
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = AppColors.surface
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Add New Item", color = AppColors.surface)
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.surface)
                        .clickable { navController.navigate(CanteenScreen.MenuListPage.name) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Menu",
                        tint = AppColors.primary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Search field
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Search menu items...",
                        color = AppColors.textTertiary
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = AppColors.textSecondary
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.primary,
                    unfocusedBorderColor = AppColors.divider,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    cursorColor = AppColors.primary,
                    focusedContainerColor = AppColors.surface,
                    unfocusedContainerColor = AppColors.surface
                )
            )

            Spacer(Modifier.height(12.dp))

            // Category Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    CategoryChip(
                        text = "All",
                        selected = selectedCategory == "All",
                        onClick = { selectedCategory = "All" }
                    )
                }

                items(CategoryData.category) { category ->
                    CategoryChip(
                        text = category.name,
                        selected = selectedCategory == category.name,
                        onClick = { selectedCategory = category.name }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Menu items list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
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

                // Empty state
                if (filteredMenuItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = AppColors.textTertiary,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = "No menu items found",
                                    color = AppColors.textSecondary,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
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
        shape = RoundedCornerShape(16.dp),
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
                tint = AppColors.surface,
                modifier = Modifier.size(28.dp)
            )

            Column {
                Text(
                    text = title,
                    color = AppColors.surface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = AppColors.surface.copy(alpha = 0.9f),
                    fontSize = 12.sp
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
        shape = RoundedCornerShape(20.dp),
        color = if (selected) AppColors.primary else AppColors.surface,
        onClick = onClick,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text,
            color = if (selected) AppColors.surface else AppColors.textSecondary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun MenuItemCard(
    item: FirestoreMenuItem,
    onEditClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() }
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
                    } catch (e: Exception) { null }
                }
            }

            bitmap?.let {
                Image(
                    it.asImageBitmap(),
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } ?: Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = AppColors.textTertiary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AppColors.textPrimary
                )
                Text(
                    item.description,
                    fontSize = 13.sp,
                    color = AppColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Stock: ${item.remainQuantity}",
                    fontSize = 12.sp,
                    color = if (item.remainQuantity > 10) AppColors.success else AppColors.warning,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.width(8.dp))

            // Price tag
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AppColors.primary.copy(alpha = 0.15f)
            ) {
                Text(
                    "RM %.2f".format(item.price),
                    color = AppColors.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
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
            selected = currentRoute == CanteenScreen.StaffOrderStatusEdit.name,
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
            selected = currentRoute == CanteenScreen.ReportScreen.name,
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