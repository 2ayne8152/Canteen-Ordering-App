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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.canteen.viewmodel.login.FirestoreMenuItem
import com.example.canteen.viewmodel.login.MenuViewModel

data class EditableMenuItem(
    val id: String,
    var name: String,
    var description: String,
    var categoryId: String,
    var price: String,
    var remainQuantity: String,
    var imageUrl: String
)

@Composable
fun MenuListPage(navController: NavController, viewModel: MenuViewModel = viewModel()) {
    val menuItems by viewModel.menuItems.collectAsState()

    val editableItems = remember(menuItems) {
        menuItems.map {
            EditableMenuItem(
                id = it.id,
                name = it.name,
                description = it.description,
                categoryId = it.categoryId,
                price = it.price.toString(),
                remainQuantity = it.remainQuantity.toString(),
                imageUrl = it.imageUrl
            )
        }.toMutableStateList()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF0D47A1))
                }
                Text("Menu List", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
            }
        }

        items(editableItems) { item ->
            MenuItemCard(item = item) { editedItem, newImageUri ->
                val index = editableItems.indexOf(item)
                if (index != -1) editableItems[index] = editedItem

                val updatedItem = FirestoreMenuItem(
                    id = editedItem.id,
                    name = editedItem.name,
                    description = editedItem.description,
                    price = editedItem.price.toDoubleOrNull() ?: 0.0,
                    categoryId = editedItem.categoryId,
                    remainQuantity = editedItem.remainQuantity.toIntOrNull() ?: 0,
                    imageUrl = editedItem.imageUrl
                )

                viewModel.updateMenuItem(updatedItem) { success, error ->
                    if (success) println("Updated successfully")
                    else println("Error: $error")
                }
            }
        }
    }
}

@Composable
fun MenuItemCard(item: EditableMenuItem, onEdit: (EditableMenuItem, Uri?) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(item.name) }
    var description by remember { mutableStateOf(item.description) }
    var category by remember { mutableStateOf(item.categoryId) }
    var price by remember { mutableStateOf(item.price) }
    var remainQty by remember { mutableStateOf(item.remainQuantity) }
    var imageUrl by remember { mutableStateOf(item.imageUrl) }
    var newImageUri by remember { mutableStateOf<Uri?>(null) }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            newImageUri = it
            imageUrl = it.toString() // local preview
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
                modifier = Modifier.height(150.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(description, fontSize = 14.sp, color = Color.DarkGray)
            Text("RM $price", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text("Quantity: $remainQty", fontSize = 14.sp, color = Color.Gray)

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Edit", color = Color.White) }
        }
    }

    if (showDialog) {
        val categories = listOf("Chicken Rice", "Curry Mee", "Tomyam Maggi")

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Edit Menu Item") },
            text = {
                Column {
                    Button(onClick = { imageLauncher.launch("image/*") }) { Text("Change Image") }
                    Spacer(Modifier.height(8.dp))
                    Text("Name"); OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Text("Description"); OutlinedTextField(value = description, onValueChange = { description = it }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Text("Category"); SimpleDropdown(categories, category) { category = it }
                    Spacer(Modifier.height(8.dp))
                    Text("Price"); OutlinedTextField(value = price, onValueChange = { price = it }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Text("Quantity"); OutlinedTextField(value = remainQty, onValueChange = { remainQty = it }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    onEdit(item.copy(
                        name = name,
                        description = description,
                        categoryId = category,
                        price = price,
                        remainQuantity = remainQty,
                        imageUrl = imageUrl
                    ), newImageUri)
                    showDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun SimpleDropdown(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(selected) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    onSelect(option)
                    expanded = false
                })
            }
        }
    }
}
