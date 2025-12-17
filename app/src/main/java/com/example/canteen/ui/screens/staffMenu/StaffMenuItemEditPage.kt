package com.example.canteen.ui.screens.staffMenu

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.canteen.viewmodel.login.MenuViewModel
import com.example.canteen.viewmodel.staffMenu.CategoryData

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

    var editedName by remember { mutableStateOf(item.name) }
    var editedDescription by remember { mutableStateOf(item.description) }
    var selectedCategory by remember { mutableStateOf(item.categoryId) }
    var editedPrice by remember { mutableStateOf(item.price.toString()) }
    var editedQuantity by remember { mutableStateOf(item.remainQuantity.toString()) }
    var editedImageUri by remember { mutableStateOf<Uri?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> editedImageUri = uri }

    // Decode image from Uri or Base64
    val bitmap = remember(editedImageUri, item.imageUrl) {
        editedImageUri?.let { uri ->
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } ?: item.imageUrl?.let {
            try {
                val bytes = Base64.decode(it, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) { null }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF0D47A1))
            }
            Text(
                "Edit Menu Item",
                fontSize = 28.sp,
                color = Color(0xFF0D47A1)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Image Picker
        Box(
            modifier = Modifier
                .height(180.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = editedName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("No Image", color = Color.Gray)
            }

            // Button to pick new image
            IconButton(
                onClick = { imageLauncher.launch("image/*") },
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Text("Save", color = Color.White)
            }

            Spacer(Modifier.height(8.dp))

            // Delete Button
            Button(
                onClick = {
                    viewModel.deleteMenuItem(item.id) { success, error ->
                        coroutineScope.launch {
                            if (success) {
                                // Show snackbar
                                snackbarHostState.showSnackbar("Menu item deleted successfully!")
                                // Navigate back to dashboard
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
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Delete", color = Color.White)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Text Fields
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
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                CategoryData.category.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            selectedCategory = category.name
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

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

        Spacer(Modifier.height(16.dp))

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
                        // Convert picked image to Base64
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            val bytes = inputStream.readBytes()
                            Base64.encodeToString(bytes, Base64.DEFAULT)
                        }
                    } ?: item.imageUrl
                )
                viewModel.updateMenuItem(updatedItem) { _, _ -> }
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
        ) {
            Text("Save", color = Color.White)
        }
    }
}