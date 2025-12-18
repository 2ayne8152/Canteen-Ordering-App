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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.canteen.ui.screens.CanteenScreen
import com.example.canteen.ui.theme.AppColors
import com.example.canteen.viewmodel.login.generateNextMenuId
import com.example.canteen.viewmodel.staffMenu.CategoryData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemForm(navController: NavController) {
    val categoryOptions = CategoryData.category.map { it.name }

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
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Menu Item",
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
            // Image Upload Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                onClick = { imagePickerLauncher.launch("image/*") }
            ) {
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth(),
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = AppColors.primary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Tap to Upload Image",
                                color = AppColors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "JPG or PNG format",
                                color = AppColors.textSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Change Image Button (when image exists)
                    if (imageUri != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                        ) {
                            FloatingActionButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
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

                    // Category Dropdown
                    var categoryExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category", color = AppColors.textSecondary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
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
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false },
                            modifier = Modifier.background(AppColors.surface)
                        ) {
                            categoryOptions.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category, color = AppColors.textPrimary) },
                                    onClick = {
                                        selectedCategory = category
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Item Name
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Item Name", color = AppColors.textSecondary) },
                        placeholder = { Text("Enter item name", color = AppColors.textTertiary) },
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

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description", color = AppColors.textSecondary) },
                        placeholder = { Text("Describe the item", color = AppColors.textTertiary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5,
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

                    // Price and Quantity Row
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
                            label = { Text("Price (RM)", color = AppColors.textSecondary) },
                            placeholder = { Text("0.00", color = AppColors.textTertiary) },
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
                            value = quantity,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.matches(Regex("^[0-9]+$"))) {
                                    quantity = input
                                }
                            },
                            label = { Text("Quantity", color = AppColors.textSecondary) },
                            placeholder = { Text("0", color = AppColors.textTertiary) },
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

            // Validation Message
            if (validationMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (validationMessage.contains("success"))
                            AppColors.success.copy(alpha = 0.2f)
                        else
                            AppColors.error.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (validationMessage.contains("success"))
                                Icons.Default.CheckCircle
                            else
                                Icons.Default.Error,
                            contentDescription = null,
                            tint = if (validationMessage.contains("success"))
                                AppColors.success
                            else
                                AppColors.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            validationMessage,
                            color = if (validationMessage.contains("success"))
                                AppColors.success
                            else
                                AppColors.error,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
            }

            // Submit Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        val priceDouble = unitPrice.toDoubleOrNull()
                        val quantityInt = quantity.toIntOrNull()

                        when {
                            itemName.isBlank() -> {
                                validationMessage = "Please enter item name"
                                return@launch
                            }
                            description.isBlank() -> {
                                validationMessage = "Please enter description"
                                return@launch
                            }
                            priceDouble == null || priceDouble <= 0 -> {
                                validationMessage = "Please enter a valid price"
                                return@launch
                            }
                            quantityInt == null || quantityInt < 0 -> {
                                validationMessage = "Please enter a valid quantity"
                                return@launch
                            }
                            imageUri == null -> {
                                validationMessage = "Please upload an image"
                                return@launch
                            }
                        }

                        validationMessage = ""
                        isLoading = true

                        try {
                            val generatedMenuId = generateNextMenuId()
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

                            validationMessage = "Menu item added successfully!"

                            // Clear form
                            itemName = ""
                            description = ""
                            unitPrice = ""
                            quantity = ""
                            imageUri = null
                            selectedCategory = categoryOptions.first()

                            // Navigate to dashboard after delay
                            kotlinx.coroutines.delay(1500)
                            navController.navigate(CanteenScreen.StaffDashboard.name) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.primary,
                    disabledContainerColor = AppColors.disabled
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = AppColors.surface,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = AppColors.surface
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Add Menu Item",
                        color = AppColors.surface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun MenuItemFormPreview() {
    val navController = rememberNavController()
    MenuItemForm(navController)
}
