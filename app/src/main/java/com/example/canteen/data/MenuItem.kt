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
    val imageUrl: String = "",
    val categoryId: String = ""
)