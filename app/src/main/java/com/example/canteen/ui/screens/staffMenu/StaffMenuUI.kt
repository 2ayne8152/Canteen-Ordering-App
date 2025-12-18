package com.example.canteen.ui.screens.staffMenu

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.net.Uri
import android.provider.MediaStore
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.canteen.ui.theme.AppColors
import com.example.canteen.viewmodel.login.generateNextMenuId
import com.example.canteen.viewmodel.staffMenu.CategoryData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemForm(navController: NavController) {
    val categoryOptions = CategoryData.category.map { it.name }

    var menuId by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categoryOptions.first()) }
    var itemName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var validationMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Menu Item",
                        color = AppColors.textPrimary,
                        style = MaterialTheme.typography.titleLarge
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
                    containerColor = AppColors.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.background)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // Menu ID
            OutlinedTextField(
                value = "Auto Generated",
                onValueChange = {},
                enabled = false,
                label = { Text("Menu ID", color = AppColors.textSecondary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledContainerColor = AppColors.surface,
                    disabledBorderColor = AppColors.divider,
                    disabledTextColor = AppColors.textSecondary,
                    disabledLabelColor = AppColors.textSecondary
                )
            )

            Spacer(Modifier.height(16.dp))

            // Category dropdown
            Text(
                "Category",
                fontSize = 16.sp,
                color = AppColors.textPrimary,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            DropdownMenuWrapper(
                options = categoryOptions,
                selectedOption = selectedCategory,
                onOptionSelected = { selectedCategory = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Item name
            OutlinedTextField(
                value = itemName,
                onValueChange = { itemName = it },
                label = { Text("Item Name", color = AppColors.textSecondary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AppColors.surface,
                    unfocusedContainerColor = AppColors.surface,
                    focusedBorderColor = AppColors.primary,
                    unfocusedBorderColor = AppColors.divider,
                    cursorColor = AppColors.primary,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    focusedLabelColor = AppColors.primary,
                    unfocusedLabelColor = AppColors.textSecondary
                )
            )

            Spacer(Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description", color = AppColors.textSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AppColors.surface,
                    unfocusedContainerColor = AppColors.surface,
                    focusedBorderColor = AppColors.primary,
                    unfocusedBorderColor = AppColors.divider,
                    cursorColor = AppColors.primary,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    focusedLabelColor = AppColors.primary,
                    unfocusedLabelColor = AppColors.textSecondary
                )
            )

            Spacer(Modifier.height(16.dp))

            // Price and Quantity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = unitPrice,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^[0-9]*\\.?[0-9]*$"))) {
                            unitPrice = input
                        }
                    },
                    label = { Text("Unit Price", color = AppColors.textSecondary) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppColors.surface,
                        unfocusedContainerColor = AppColors.surface,
                        focusedBorderColor = AppColors.primary,
                        unfocusedBorderColor = AppColors.divider,
                        cursorColor = AppColors.primary,
                        focusedTextColor = AppColors.textPrimary,
                        unfocusedTextColor = AppColors.textPrimary,
                        focusedLabelColor = AppColors.primary,
                        unfocusedLabelColor = AppColors.textSecondary
                    )
                )

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^[0-9]+$"))) {
                            quantity = input
                        }
                    },
                    label = { Text("Quantity", color = AppColors.textSecondary) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppColors.surface,
                        unfocusedContainerColor = AppColors.surface,
                        focusedBorderColor = AppColors.primary,
                        unfocusedBorderColor = AppColors.divider,
                        cursorColor = AppColors.primary,
                        focusedTextColor = AppColors.textPrimary,
                        unfocusedTextColor = AppColors.textPrimary,
                        focusedLabelColor = AppColors.primary,
                        unfocusedLabelColor = AppColors.textSecondary
                    )
                )
            }

            Spacer(Modifier.height(20.dp))

            // Image picker
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.primary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    "Upload Image",
                    color = AppColors.surface,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Preview",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .height(180.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.divider),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri != null) {
                            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
                            } else {
                                val source = ImageDecoder.createSource(context.contentResolver, imageUri!!)
                                ImageDecoder.decodeBitmap(source)
                            }
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                "No Image",
                                color = AppColors.textSecondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    PreviewTextRow("Menu ID", menuId.ifEmpty { "Auto Generated" })
                    PreviewTextRow("Category", selectedCategory)
                    PreviewTextRow("Name", itemName.ifEmpty { "Item Name" })
                    PreviewTextRow("Description", description.ifEmpty { "Description" })
                    PreviewTextRow("Price", "RM ${unitPrice.ifEmpty { "0.00" }}")
                    PreviewTextRow("Remaining Quantity", quantity.ifEmpty { "0" })
                }
            }

            Spacer(Modifier.height(16.dp))

            if (validationMessage.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (validationMessage.contains("success", ignoreCase = true))
                        AppColors.success.copy(alpha = 0.15f)
                    else
                        AppColors.error.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = validationMessage,
                        color = if (validationMessage.contains("success", ignoreCase = true))
                            AppColors.success
                        else
                            AppColors.error,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Submit button
            Button(
                onClick = {
                    coroutineScope.launch {
                        val priceDouble = unitPrice.toDoubleOrNull()
                        val quantityInt = quantity.toIntOrNull()

                        when {
                            itemName.isBlank() || description.isBlank() -> {
                                validationMessage = "All text fields must be filled."
                                return@launch
                            }
                            priceDouble == null -> {
                                validationMessage = "Unit Price must be a valid number."
                                return@launch
                            }
                            quantityInt == null -> {
                                validationMessage = "Quantity must be an integer."
                                return@launch
                            }
                            imageUri == null -> {
                                validationMessage = "Please upload an image."
                                return@launch
                            }
                        }

                        validationMessage = ""
                        isLoading = true

                        try {
                            val generatedMenuId = generateNextMenuId()
                            // Convert imageUri to Base64
                            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                                MediaStore.Images.Media.getBitmap(
                                    context.contentResolver, imageUri
                                )
                            } else {
                                val source = ImageDecoder.createSource(
                                    context.contentResolver, imageUri!!
                                )
                                ImageDecoder.decodeBitmap(source)
                            }

                            val outputStream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                            val imageBase64 = Base64.encodeToString(
                                outputStream.toByteArray(),
                                Base64.DEFAULT
                            )

                            val newMenuItem = mapOf(
                                "id" to generatedMenuId,
                                "name" to itemName,
                                "description" to description,
                                "price" to priceDouble,
                                "remainQuantity" to quantityInt,
                                "categoryId" to selectedCategory,
                                "imageUrl" to imageBase64
                            )

                            Firebase.firestore
                                .collection("MenuItems")
                                .document(generatedMenuId)
                                .set(newMenuItem)
                                .await()

                            validationMessage = "Menu item added successfully! ($generatedMenuId)"
                            menuId = ""
                            itemName = ""
                            description = ""
                            unitPrice = ""
                            quantity = ""
                            imageUri = null

                            // Navigate to StaffDashboard after 1 second
                            kotlinx.coroutines.delay(1000)
                            navController.navigate("StaffDashboard") {
                                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                launchSingleTop = true
                            }
                        } catch (e: Exception) {
                            validationMessage = "Error: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = AppColors.surface,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Submit",
                        color = AppColors.surface,
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun PreviewTextRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            "$label: ",
            fontSize = 14.sp,
            color = AppColors.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            value,
            fontSize = 14.sp,
            color = AppColors.textPrimary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
fun DropdownMenuWrapper(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = AppColors.surface,
                contentColor = AppColors.textPrimary
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.SolidColor(AppColors.divider)
            )
        ) {
            Text(
                selectedOption,
                color = AppColors.textPrimary,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.surface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option,
                            color = AppColors.textPrimary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = AppColors.textPrimary
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun MenuItemFormPreview() {
    val navController = rememberNavController()
    MenuItemForm(navController)
}