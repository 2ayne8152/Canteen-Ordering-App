package com.example.canteen.ui.screens.staffMenu

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.canteen.viewmodel.staffMenu.CategoryData

// Menu item model
data class MenuItem(
    var name: String,
    var description: String,
    var category: String,
    var price: String,
    var availability: String,
    var imageUri: String? = null
)

@Composable
fun MenuListPage() {

    // Dummy data
    val menuItems = remember {
        mutableStateListOf(
            MenuItem("Coca Cola", "Refreshing drink", "Beverages", "5.00", "Available"),
            MenuItem("Burger", "Beef burger with cheese", "Food", "12.00", "Unavailable"),
            MenuItem("Chocolate Cake", "Delicious dessert", "Desserts", "8.50", "Available")
        )
    }

    // Built-in beautiful font
    val beautifulFont = FontFamily.Serif

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title with divider
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Menu List",
                    fontSize = 28.sp,
                    fontFamily = beautifulFont,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D47A1)
                )
                Divider(
                    color = Color(0xFF0D47A1),
                    thickness = 2.dp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
            }
        }

        // Menu items
        items(menuItems) { item ->
            MenuItemCard(item = item, onEdit = { editedItem ->
                val index = menuItems.indexOf(item)
                if (index != -1) menuItems[index] = editedItem
            })
        }
    }
}

@Composable
fun MenuItemCard(item: MenuItem, onEdit: (MenuItem) -> Unit) {
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // Image placeholder
            Box(
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
                    .background(Color.LightGray, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!item.imageUri.isNullOrEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(item.imageUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("No Image")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Name: ${item.name}", fontSize = 16.sp)
            Text("Description: ${item.description}", fontSize = 14.sp)
            Text("Category: ${item.category}", fontSize = 14.sp)
            Text("Price: RM ${item.price}", fontSize = 14.sp)
            Text("Status: ${item.availability}", fontSize = 14.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { showEditDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Edit", color = Color.White)
            }
        }
    }

    if (showEditDialog) {
        var editedName by remember { mutableStateOf(item.name) }
        var editedDescription by remember { mutableStateOf(item.description) }
        var editedCategory by remember { mutableStateOf(item.category) }
        var editedPrice by remember { mutableStateOf(item.price) }
        var editedAvailability by remember { mutableStateOf(item.availability) }
        var editedImageUri by remember { mutableStateOf<Uri?>(item.imageUri?.let { Uri.parse(it) }) }

        val categoryOptions = CategoryData.categories.map { it.name }
        val availabilityOptions = listOf("Available", "Unavailable")

        val imageLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            editedImageUri = uri
        }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Item") },
            text = {
                Column {
                    // Image picker
                    Box(
                        modifier = Modifier
                            .height(150.dp)
                            .fillMaxWidth()
                            .background(Color.LightGray, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (editedImageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(editedImageUri),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text("No Image")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { imageLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
                    ) {
                        Text("Change Image", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editedDescription,
                        onValueChange = { editedDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Category")
                    SimpleDropdown(
                        options = categoryOptions,
                        selectedOption = editedCategory,
                        onOptionSelected = { editedCategory = it }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editedPrice,
                        onValueChange = { editedPrice = it },
                        label = { Text("Price") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Availability")
                    SimpleDropdown(
                        options = availabilityOptions,
                        selectedOption = editedAvailability,
                        onOptionSelected = { editedAvailability = it }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onEdit(
                        item.copy(
                            name = editedName,
                            description = editedDescription,
                            category = editedCategory,
                            price = editedPrice,
                            availability = editedAvailability,
                            imageUri = editedImageUri?.toString()
                        )
                    )
                    showEditDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SimpleDropdown(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(selectedOption.ifEmpty { "Select" })
        }

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

@Composable
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
fun MenuListPagePreview() {
    MenuListPage()
}
