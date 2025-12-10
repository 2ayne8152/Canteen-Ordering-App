package com.example.canteen.ui.screens.staffMenu

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.canteen.data.MenuItem
import com.example.canteen.data.menuItems
import com.example.canteen.viewmodel.staffMenu.CategoryData
import com.example.menumanagement.categories
import com.example.canteen.viewmodel.staffMenu.Category
import com.example.canteen.viewmodel.staffMenu.CategoryData.category

// -------------------------------
// Editable wrapper for MenuItem
// -------------------------------
data class MenuItemEditable(
    val nameRes: Int,
    val descriptionRes: Int,
    var category: String = "Food",
    var price: String = "0.0",
    var availability: String = "Available",
    val imageRes: Int,
    var imageUri: String? = null
)

// -------------------------------
// Main Menu List Page
// -------------------------------
@Composable
fun MenuListPage() {
    val beautifulFont = FontFamily.Serif

    // Wrap menuItems in a mutable state list to allow editing
    val editableMenuItems = remember {
        menuItems.map { item ->
            MenuItemEditable(
                nameRes = item.itemName,
                descriptionRes = item.itemDesc,
                price = item.itemPrice.toString(),
                imageRes = item.imageRes
            )
        }.toMutableStateList()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title
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

        // Menu Items
        items(editableMenuItems) { item ->
            MenuItemCard(item = item, onEdit = { editedItem ->
                val index = editableMenuItems.indexOf(item)
                if (index != -1) editableMenuItems[index] = editedItem
            })
        }
    }
}

// -------------------------------
// Menu Item Card with Edit
// -------------------------------
@Composable
fun MenuItemCard(item: MenuItemEditable, onEdit: (MenuItemEditable) -> Unit) {
    var showEditDialog by remember { mutableStateOf(false) }

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
                if (!item.imageUri.isNullOrEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(item.imageUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = item.imageRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(stringResource(id = item.nameRes), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(stringResource(id = item.descriptionRes), fontSize = 14.sp, color = Color.DarkGray)
            Text("RM ${item.price}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text("Status: ${item.availability}", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { showEditDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Edit", color = Color.White)
            }
        }
    }

    if (showEditDialog) {
        var editedCategory by remember { mutableStateOf(item.category) }
        var editedPrice by remember { mutableStateOf(item.price) }
        var editedAvailability by remember { mutableStateOf(item.availability) }
        var editedImageUri by remember { mutableStateOf<Uri?>(item.imageUri?.let { Uri.parse(it) }) }

        val categoryOptions = CategoryData.category
        val availabilityOptions = listOf("Available", "Unavailable")

        val imageLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            editedImageUri = uri
        }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Menu Item") },
            text = {
                Column {
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
                            Image(
                                painter = painterResource(id = item.imageRes),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
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

                    Text("Category")
                    SimpleDropdown(
                        options = category.map { it.name },
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
                    onEdit(item.copy(
                        category = editedCategory,
                        price = editedPrice,
                        availability = editedAvailability,
                        imageUri = editedImageUri?.toString()
                    ))
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

// -------------------------------
// Simple Dropdown Composable
// -------------------------------
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

// -------------------------------
// Preview
// -------------------------------
@Composable
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
fun MenuListPagePreview() {
    MenuListPage()
}
