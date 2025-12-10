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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.canteen.viewmodel.staffMenu.CategoryData

@Composable
fun MenuItemForm(navController: NavController) {

    val categoryOptions = CategoryData.category.map { it.name }

    var selectedCategory by remember { mutableStateOf(categoryOptions.first()) }
    var itemName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("Available") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val availabilityOptions = listOf("Available", "Unavailable")

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {  // BACK BUTTON
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() },
                modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
            }
        }

        Spacer(Modifier.height(8.dp))

        // CATEGORY DROPDOWN
        Text("Category", fontSize = 16.sp)
        DropdownMenuWrapper(
            options = categoryOptions,
            selectedOption = selectedCategory,
            onOptionSelected = { selectedCategory = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ITEM NAME INPUT
        TextField(
            value = itemName,
            onValueChange = { itemName = it },
            label = { Text("Item Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // DESCRIPTION
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

        Spacer(modifier = Modifier.height(12.dp))

        // PRICE + AVAILABILITY
        Row(modifier = Modifier.fillMaxWidth()) {

            TextField(
                value = unitPrice,
                onValueChange = { unitPrice = it },
                label = { Text("Unit Price") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            DropdownMenuWrapper(
                options = availabilityOptions,
                selectedOption = availability,
                onOptionSelected = { availability = it },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // IMAGE PICKER
        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.align(Alignment.Start),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Upload Image")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // =====================
        // PREVIEW SECTION
        // =====================
        Text("Preview", fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {

                // IMAGE
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

                Spacer(modifier = Modifier.height(12.dp))

                // TEXT PREVIEW
                PreviewTextRow(label = "Category", value = selectedCategory)
                PreviewTextRow(label = "Name", value = itemName.ifEmpty { "Item Name" })
                PreviewTextRow(label = "Description", value = description.ifEmpty { "Description" })
                PreviewTextRow(label = "Price", value = "RM ${unitPrice.ifEmpty { "0.00" }}")
                PreviewTextRow(label = "Status", value = availability)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // =====================
        // SUBMIT BUTTON
        // =====================
        Button(
            onClick = {
                // TODO: Add database save logic here
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Submit", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
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

    Box(modifier = modifier) {

        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(selectedOption)
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

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun MenuItemFormPreview() {
    val navController = rememberNavController()
    MenuItemForm(navController)
}

