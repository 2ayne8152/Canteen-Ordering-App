package com.example.canteen.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.canteen.R


data class MenuItem(
    val id: String = "",
    val categoryId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val remainQuantity: Int = 0,
    val imageUrl: String = ""
)



// For Testing Only, will retrieve from Firebase in future
val menuItems = listOf(
    MenuItem(
        id = "M0001",
        categoryId = "C0001",
        name = "Chicken Rice",
        description = "Steamed chicken served with fragrant rice",
        price = 6.00,
        remainQuantity = 5,
        imageUrl = "https://example.com/images/chicken_rice.jpg"
    ),
    MenuItem(
        id = "M0002",
        categoryId = "C0002",
        name = "Tomyam Maggi",
        description = "Spicy tomyam flavoured Maggi mee",
        price = 8.00,
        remainQuantity = 5,
        imageUrl = "https://example.com/images/tomyam_maggi.jpg"
    ),
    MenuItem(
        id = "M0003",
        categoryId = "C0003",
        name = "Curry Mee",
        description = "Curry soup with noodles",
        price = 10.00,
        remainQuantity = 5,
        imageUrl = "https://example.com/images/curry_mee.jpg"
    )
)
