package com.example.canteen.ui.screens.staffMenu

import android.net.Uri
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.canteen.viewmodel.staffMenu.CategoryData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.storage.ktx.storage
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID


@Composable
fun MenuItemForm(navController: NavController) {

    val categoryOptions = CategoryData.category.map { it.name }

    // -------------------- States --------------------
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        // -------------------- BACK BUTTON --------------------
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
            }
        }

        Spacer(Modifier.height(8.dp))

        // -------------------- MENU ID --------------------
        TextField(
            value = menuId,
            onValueChange = { menuId = it },
            label = { Text("Menu ID") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(12.dp))

        // -------------------- CATEGORY DROPDOWN --------------------
        Text("Category", fontSize = 16.sp)
        DropdownMenuWrapper(
            options = categoryOptions,
            selectedOption = selectedCategory,
            onOptionSelected = { selectedCategory = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // -------------------- ITEM NAME --------------------
        TextField(
            value = itemName,
            onValueChange = { itemName = it },
            label = { Text("Item Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(12.dp))

        // -------------------- DESCRIPTION --------------------
        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(12.dp))

        // -------------------- PRICE + QUANTITY --------------------
        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = unitPrice,
                onValueChange = { input ->
                    if (input.isEmpty() || input.matches(Regex("^[0-9]*\\.?[0-9]*$"))) {
                        unitPrice = input
                    }
                },
                label = { Text("Unit Price") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.width(8.dp))

            TextField(
                value = quantity,
                onValueChange = { input ->
                    if (input.isEmpty() || input.matches(Regex("^[0-9]+$"))) {
                        quantity = input
                    }
                },
                label = { Text("Quantity") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        // -------------------- IMAGE PICKER --------------------
        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.align(Alignment.Start),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Upload Image")
        }

        Spacer(Modifier.height(12.dp))

        // -------------------- PREVIEW --------------------
        Text("Preview", fontSize = 18.sp)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Box(
                    modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth()
                        .background(Color.LightGray, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("No Image")
                    }
                }

                Spacer(Modifier.height(12.dp))

                PreviewTextRow("Menu ID", menuId.ifEmpty { "ID" })
                PreviewTextRow("Category", selectedCategory)
                PreviewTextRow("Name", itemName.ifEmpty { "Item Name" })
                PreviewTextRow("Description", description.ifEmpty { "Description" })
                PreviewTextRow("Price", "RM ${unitPrice.ifEmpty { "0.00" }}")
                PreviewTextRow("Remaining Quantity", quantity.ifEmpty { "0" })
            }
        }

        Spacer(Modifier.height(16.dp))

        // -------------------- VALIDATION MESSAGE --------------------
        if (validationMessage.isNotEmpty()) {
            Text(
                text = validationMessage,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // -------------------- SUBMIT BUTTON --------------------
        Button(
            onClick = {
                coroutineScope.launch {
                    val priceDouble = unitPrice.toDoubleOrNull()
                    val quantityInt = quantity.toIntOrNull()

                    when {
                        menuId.isBlank() || itemName.isBlank() || description.isBlank() -> {
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
                        // Upload image to Firebase Storage
                        val storageRef = Firebase.storage.reference
                        val fileName = "menu_images/${UUID.randomUUID()}"
                        val imageRef = storageRef.child(fileName)
                        imageRef.putFile(imageUri!!).await()
                        val imageUrl = imageRef.downloadUrl.await().toString()

                        // Save menu item to Firestore
                        val newMenuItem = mapOf(
                            "id" to menuId,
                            "name" to itemName,
                            "description" to description,
                            "price" to priceDouble,
                            "remainQuantity" to quantityInt,
                            "categoryId" to selectedCategory,
                            "imageUrl" to imageUrl
                        )

                        Firebase.firestore.collection("menu_items")
                            .document(menuId)
                            .set(newMenuItem)
                            .await()

                        validationMessage = "Menu item added successfully!"
                        // Optionally, clear form fields here
                        menuId = ""
                        itemName = ""
                        description = ""
                        unitPrice = ""
                        quantity = ""
                        imageUri = null

                    } catch (e: Exception) {
                        validationMessage = "Error: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Submit", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun PreviewTextRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", fontSize = 14.sp, color = Color.DarkGray)
        Text(value, fontSize = 14.sp, color = Color.Black)
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
            shape = RoundedCornerShape(12.dp)
        ) { Text(selectedOption) }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
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
