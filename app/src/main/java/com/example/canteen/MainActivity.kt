package com.example.canteen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.canteen.data.MenuItem
import com.example.canteen.ui.screens.UserHomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Sample menu items
                    val sampleMenu = listOf(
                        MenuItem(
                            id = "1",
                            name = "Cheeseburger",
                            description = "Juicy beef patty with cheese",
                            price = 8.50,
                            remainQuantity = 10,
                            imageUrl = ""
                        ),
                        MenuItem(
                            id = "2",
                            name = "Veggie Wrap",
                            description = "Healthy veggie wrap with hummus",
                            price = 6.00,
                            remainQuantity = 5,
                            imageUrl = ""
                        ),
                        MenuItem(
                            id = "3",
                            name = "French Fries",
                            description = "Crispy golden fries",
                            price = 3.50,
                            remainQuantity = 20,
                            imageUrl = ""
                        )
                    )

                    UserHomeScreen(
                        menuItems = sampleMenu,
                        numOfItem = 0,
                        totalPrice = 0.0,
                        onItemClick = {},
                        onDetailClick = {}
                    )
                }
            }
        }
    }
}
