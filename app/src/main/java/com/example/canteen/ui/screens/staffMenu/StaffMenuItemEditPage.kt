package com.example.canteen.ui.screens.staffMenu

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.canteen.R
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
    var editedPrice by remember { mutableStateOf(String.format("%.2f", item.price)) }
    var editedQuantity by remember { mutableStateOf(item.remainQuantity.toString()) }
    var editedImageUri by remember { mutableStateOf<Uri?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }


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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Menu Item",
                        color = AppColors.textPrimary,
                        fontWeight = FontWeight.SemiBold
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
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppColors.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Image Picker
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = editedName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.tomyammaggi),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    FloatingActionButton(
                        onClick = { imageLauncher.launch("image/*") },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp),
                        containerColor = AppColors.primary,
                        contentColor = AppColors.surface
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Pick Image")
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Form Fields
            OutlinedTextField(
                value = editedName,
                onValueChange = { editedName = it },
                label = { Text("Name", color = AppColors.textSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.primary,
                    unfocusedBorderColor = AppColors.divider,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    cursorColor = AppColors.primary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = editedDescription,
                onValueChange = { editedDescription = it },
                label = { Text("Description", color = AppColors.textSecondary) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.primary,
                    unfocusedBorderColor = AppColors.divider,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    cursorColor = AppColors.primary
                ),
                shape = RoundedCornerShape(12.dp)
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
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.primary,
                        unfocusedBorderColor = AppColors.divider,
                        focusedTextColor = AppColors.textPrimary,
                        unfocusedTextColor = AppColors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(AppColors.surface)
                ) {
                    CategoryData.category.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name, color = AppColors.textPrimary) },
                            onClick = {
                                selectedCategory = category.name
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = editedPrice,
                onValueChange = { input ->
                    // If empty, keep empty
                    if (input.isEmpty()) {
                        editedPrice = ""
                        return@OutlinedTextField
                    }

                    // Allow only digits and ONE dot
                    if (input.count { it == '.' } > 1) return@OutlinedTextField

                    val filtered = input.filter { it.isDigit() || it == '.' }

                    // Prevent starting with dot
                    if (filtered == ".") {
                        editedPrice = "0."
                        return@OutlinedTextField
                    }

                    editedPrice = filtered
                },
                label = { Text("Price (RM)", color = AppColors.textSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.primary,
                    unfocusedBorderColor = AppColors.divider,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    cursorColor = AppColors.primary
                ),
                shape = RoundedCornerShape(12.dp)
            )


            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = editedQuantity,
                onValueChange = { input ->
                    if (input.isEmpty()) {
                        editedQuantity = ""
                        return@OutlinedTextField
                    }

                    if (!input.all { it.isDigit() }) return@OutlinedTextField

                    val number = input.toIntOrNull() ?: return@OutlinedTextField
                    if (number <= 10000) {
                        editedQuantity = input
                    }
                },
                label = { Text("Quantity", color = AppColors.textSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.primary,
                    unfocusedBorderColor = AppColors.divider,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    cursorColor = AppColors.primary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = { showSaveDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.primary
                ),
                shape = RoundedCornerShape(50.dp)
            ) {
                Text(
                    "Save Changes",
                    color = AppColors.surface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(12.dp))

            if (showSaveDialog) {
                AlertDialog(
                    onDismissRequest = { showSaveDialog = false },
                    title = { Text("Confirm Save") },
                    text = { Text("Are you sure you want to save changes to this menu item?") },
                    confirmButton = {
                        TextButton(
                            onClick = { if (editedName.isBlank() ||
                                editedPrice.isBlank() ||
                                editedQuantity.isBlank()
                            ) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Please fill in all required fields")
                                }
                                return@TextButton
                            }
                                showSaveDialog = false
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
                                            navController.navigate("StaffDashboard") {
                                                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                                launchSingleTop = true
                                            }
                                        } else {
                                            snackbarHostState.showSnackbar("Update failed: ${error ?: "Unknown error"}")
                                        }
                                    }
                                }
                            }
                        ) {
                            Text("Save", color = AppColors.primary)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSaveDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            // Delete Button
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AppColors.error
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.5.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(AppColors.error)
                ),
                shape = RoundedCornerShape(50.dp)
            ) {
                Text(
                    "Delete Item",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(16.dp))

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Menu Item") },
                    text = { Text("Are you sure you want to delete this item? This action cannot be undone.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
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
                                        }
                                    }
                                }
                            }
                        ) {
                            Text("Delete", color = AppColors.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}