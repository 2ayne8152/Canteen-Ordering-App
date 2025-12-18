
package com.example.canteen.ui.screens.staffMenu

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.canteen.ui.screens.CanteenScreen
import com.example.canteen.ui.theme.AppColors
import com.example.canteen.viewmodel.login.MenuViewModel
import com.example.canteen.viewmodel.staffMenu.CategoryData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffMenuItemEditPage(
    itemId: String,
    navController: NavController,
    viewModel: MenuViewModel = viewModel()
) {
    val menuItems by viewModel.menuItems.collectAsState()
    val item = menuItems.find { it.id == itemId } ?: return

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var editedName by remember { mutableStateOf(item.name) }
    var editedDescription by remember { mutableStateOf(item.description) }
    var selectedCategory by remember { mutableStateOf(item.categoryId) }
    var editedPrice by remember { mutableStateOf(item.price.toString()) }
    var editedQuantity by remember { mutableStateOf(item.remainQuantity.toString()) }
    var editedImageUri by remember { mutableStateOf<Uri?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> editedImageUri = uri }

    val bitmap = remember(editedImageUri, item.imageUrl) {
        editedImageUri?.let { uri ->
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        } ?: item.imageUrl?.let {
            try {
                val bytes = Base64.decode(it, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) { null }
        }
    }

    Scaffold(
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Menu Item",
                        color = AppColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = AppColors.textPrimary
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
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = AppColors.surface,
                    contentColor = AppColors.textPrimary,
                    actionColor = AppColors.primary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Image Picker Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = editedName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(AppColors.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = AppColors.textTertiary,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }

                    // Edit Image Button
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        FloatingActionButton(
                            onClick = { imageLauncher.launch("image/*") },
                            containerColor = AppColors.primary,
                            contentColor = AppColors.surface,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Change Image"
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Form Fields Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Item Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary
                    )

                    Spacer(Modifier.height(16.dp))

                    // Name Field
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Item Name", color = AppColors.textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.primary,
                            unfocusedBorderColor = AppColors.divider,
                            focusedTextColor = AppColors.textPrimary,
                            unfocusedTextColor = AppColors.textPrimary,
                            cursorColor = AppColors.primary,
                            focusedContainerColor = AppColors.background,
                            unfocusedContainerColor = AppColors.background
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    // Description Field
                    OutlinedTextField(
                        value = editedDescription,
                        onValueChange = { editedDescription = it },
                        label = { Text("Description", color = AppColors.textSecondary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.primary,
                            unfocusedBorderColor = AppColors.divider,
                            focusedTextColor = AppColors.textPrimary,
                            unfocusedTextColor = AppColors.textPrimary,
                            cursorColor = AppColors.primary,
                            focusedContainerColor = AppColors.background,
                            unfocusedContainerColor = AppColors.background
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    // Category Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category", color = AppColors.textSecondary) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = expanded
                                )
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.primary,
                                unfocusedBorderColor = AppColors.divider,
                                focusedTextColor = AppColors.textPrimary,
                                unfocusedTextColor = AppColors.textPrimary,
                                focusedContainerColor = AppColors.background,
                                unfocusedContainerColor = AppColors.background
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(AppColors.surface)
                        ) {
                            CategoryData.category.forEach { category ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            category.name,
                                            color = AppColors.textPrimary
                                        )
                                    },
                                    onClick = {
                                        selectedCategory = category.name
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Price and Quantity Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = editedPrice,
                            onValueChange = {
                                if (it.isEmpty() || it.matches(Regex("^[0-9]*\\.?[0-9]*$"))) {
                                    editedPrice = it
                                }
                            },
                            label = { Text("Price (RM)", color = AppColors.textSecondary) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.primary,
                                unfocusedBorderColor = AppColors.divider,
                                focusedTextColor = AppColors.textPrimary,
                                unfocusedTextColor = AppColors.textPrimary,
                                cursorColor = AppColors.primary,
                                focusedContainerColor = AppColors.background,
                                unfocusedContainerColor = AppColors.background
                            )
                        )

                        OutlinedTextField(
                            value = editedQuantity,
                            onValueChange = {
                                if (it.isEmpty() || it.matches(Regex("^[0-9]+$"))) {
                                    editedQuantity = it
                                }
                            },
                            label = { Text("Quantity", color = AppColors.textSecondary) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.primary,
                                unfocusedBorderColor = AppColors.divider,
                                focusedTextColor = AppColors.textPrimary,
                                unfocusedTextColor = AppColors.textPrimary,
                                cursorColor = AppColors.primary,
                                focusedContainerColor = AppColors.background,
                                unfocusedContainerColor = AppColors.background
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Save Button
            Button(
                onClick = {
                    val updatedItem = item.copy(
                        name = editedName,
                        description = editedDescription,
                        categoryId = selectedCategory,
                        price = editedPrice.toDoubleOrNull() ?: 0.0,
                        remainQuantity = editedQuantity.toIntOrNull() ?: 0,
                        imageUrl = editedImageUri?.let { uri ->
                            context.contentResolver.openInputStream(uri)?.use { input ->
                                val bytes = input.readBytes()
                                Base64.encodeToString(bytes, Base64.DEFAULT)
                            }
                        } ?: item.imageUrl
                    )

                    viewModel.updateMenuItem(updatedItem) { success, error ->
                        coroutineScope.launch {
                            if (success) {
                                snackbarHostState.showSnackbar("Menu item updated successfully!")
                                navController.navigate(CanteenScreen.StaffDashboard.name) {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                    launchSingleTop = true
                                }
                            } else {
                                snackbarHostState.showSnackbar("Update failed: ${error ?: "Unknown error"}")
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.primary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = AppColors.surface
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Save Changes",
                    color = AppColors.surface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            // Delete Button
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AppColors.error
                ),
                border = BorderStroke(1.dp, AppColors.error),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Delete Item",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Delete Menu Item?",
                    color = AppColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${item.name}\"? This action cannot be undone.",
                    color = AppColors.textSecondary
                )
            },
            confirmButton = {
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
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.error
                    )
                ) {
                    Text("Delete", color = AppColors.surface)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = AppColors.textSecondary)
                }
            },
            containerColor = AppColors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
