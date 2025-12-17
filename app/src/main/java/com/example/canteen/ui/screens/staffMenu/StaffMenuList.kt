package com.example.canteen.ui.screens.staffMenu

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.canteen.ui.screens.CanteenScreen
import com.example.canteen.viewmodel.login.FirestoreMenuItem
import com.example.canteen.viewmodel.login.MenuViewModel
import com.example.canteen.viewmodel.staffMenu.CategoryData
import kotlinx.coroutines.launch

@Composable
fun StaffMenuListPage(
    navController: NavController,
    viewModel: MenuViewModel = viewModel()
) {
    val menuItems by viewModel.menuItems.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }  // Snackbar host

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0D47A1)
                        )
                    }
                    Text(
                        text = "Menu List",
                        fontSize = 28.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1)
                    )
                }
            }

            // Menu Items
            items(menuItems) { item ->
                StaffMenuItemCard(
                    item = item,
                    viewModel = viewModel,
                    navController = navController,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}

@Composable
fun StaffMenuItemCard(
    item: FirestoreMenuItem,
    viewModel: MenuViewModel,
    navController: NavController,
    snackbarHostState: SnackbarHostState
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(item.name) }
    var editedDescription by remember { mutableStateOf(item.description) }
    var editedCategory by remember { mutableStateOf(item.categoryId) }
    var editedPrice by remember { mutableStateOf(item.price.toString()) }
    var editedQuantity by remember { mutableStateOf(item.remainQuantity.toString()) }
    var editedImageUri by remember { mutableStateOf<Uri?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        editedImageUri = uri
    }

    // Decode Base64 image
    val bitmap = remember(editedImageUri, item.imageUrl) {
        editedImageUri?.let { null }
            ?: item.imageUrl?.let { base64 ->
                try {
                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } catch (e: Exception) {
                    null
                }
            }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = editedName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(editedName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(editedDescription, fontSize = 14.sp, color = Color.DarkGray)
            Text(
                "RM ${String.format("%.2f", editedPrice.toDoubleOrNull() ?: 0.0)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text("Quantity: $editedQuantity", fontSize = 14.sp, color = Color.Gray)

            Spacer(Modifier.height(8.dp))

            // Edit Button
            Button(
                onClick = { showEditDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Edit", color = Color.White)
            }

            Spacer(Modifier.height(4.dp))

            // Delete Button
            Button(
                onClick = {
                    viewModel.deleteMenuItem(item.id) { success, error ->
                        coroutineScope.launch {
                            if (success) {
                                snackbarHostState.showSnackbar("Menu item deleted successfully!")
                                navController.navigate(CanteenScreen.StaffDashboard.name) {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                    launchSingleTop = true
                                }
                            } else {
                                snackbarHostState.showSnackbar("Delete failed: ${error ?: "Unknown error"}")
                                Log.e("DeleteItem", error ?: "Unknown error")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Delete Item", color = Color.White)
            }
        }
    }

    // Edit Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Menu Item") },
            text = {
                Column {
                    Box(
                        modifier = Modifier
                            .height(150.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        bitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = editedName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { imageLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
                    ) {
                        Text("Change Image", color = Color.White)
                    }

                    Spacer(Modifier.height(8.dp))

                    // Name & Description
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editedDescription,
                        onValueChange = { editedDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    // Category Dropdown
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(editedCategory)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            CategoryData.category.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        editedCategory = category.name
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Price & Quantity
                    OutlinedTextField(
                        value = editedPrice,
                        onValueChange = { editedPrice = it },
                        label = { Text("Price") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editedQuantity,
                        onValueChange = { editedQuantity = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val updatedItem = item.copy(
                        name = editedName,
                        description = editedDescription,
                        categoryId = editedCategory,
                        price = String.format("%.2f", editedPrice.toDoubleOrNull() ?: 0.0).toDouble(),
                        remainQuantity = editedQuantity.toIntOrNull() ?: 0,
                        imageUrl = editedImageUri?.let { null } ?: item.imageUrl
                    )
                    viewModel.updateMenuItem(updatedItem) { success, error ->
                        if (!success) Log.e("UpdateItem", error ?: "Unknown error")
                    }
                    showEditDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                Button(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }
}
