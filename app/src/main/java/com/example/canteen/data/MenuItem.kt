package com.example.canteen.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.canteen.R

data class MenuItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val remainQuantity: Int = 0,
    val imageUrl: String = ""
)

val sampleMenu = listOf(
    MenuItem(
        id = "1",
        name = "Cheeseburger",
        description = "Juicy beef patty with melted cheese",
        price = 8.50,
        remainQuantity = 10,
        imageUrl = "" // You can put a real URL here for testing, e.g., "https://via.placeholder.com/150"
    ),
    MenuItem(
        id = "2",
        name = "Veggie Wrap",
        description = "Healthy wrap with fresh vegetables and hummus",
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
    ),
    MenuItem(
        id = "4",
        name = "Chocolate Muffin",
        description = "Soft muffin filled with chocolate chips",
        price = 4.00,
        remainQuantity = 15,
        imageUrl = ""
    ),
    MenuItem(
        id = "5",
        name = "Iced Coffee",
        description = "Refreshing iced coffee with milk",
        price = 5.00,
        remainQuantity = 25,
        imageUrl = ""
    )
)
