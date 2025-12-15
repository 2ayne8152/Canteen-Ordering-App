package com.example.canteen.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.canteen.R

data class MenuItem(
    val menuId: String,
    val categoryId : String,
    @DrawableRes val imageRes: Int,
    @StringRes val itemName: Int,
    @StringRes val itemDesc: Int,
    val itemPrice: Double,
    val remainQuantity: Int
)

// For Testing Only, will retrieve from database in future
val menuItems = listOf(
    MenuItem(
        menuId = "M0001",
        categoryId = "C0001",
        imageRes = R.drawable.chickenrice,
        itemName = R.string.chicken_rice,
        itemDesc = R.string.chicken_and_rice,
        itemPrice = 6.00,
        remainQuantity = 5
    ),
    MenuItem(
        menuId = "M0002",
        categoryId = "C0002",
        imageRes = R.drawable.tomyammaggi,
        itemName = R.string.tomyam_maggi,
        itemDesc = R.string.tomyam_flavour_maggi_mee,
        itemPrice = 8.00,
        remainQuantity = 5
    ),
    MenuItem(
        menuId = "M0003",
        categoryId = "C0003",
        imageRes = R.drawable.currymee,
        itemName = R.string.curry_mee,
        itemDesc = R.string.curry_and_mee,
        itemPrice = 10.00,
        remainQuantity = 5
    ),
    MenuItem(
        menuId = "M0004",
        categoryId = "C0001",
        imageRes = R.drawable.chickenrice,
        itemName = R.string.chicken_rice,
        itemDesc = R.string.chicken_and_rice,
        itemPrice = 6.00,
        remainQuantity = 5
    ),
    MenuItem(
        menuId = "M0005",
        categoryId = "C0002",
        imageRes = R.drawable.tomyammaggi,
        itemName = R.string.tomyam_maggi,
        itemDesc = R.string.tomyam_flavour_maggi_mee,
        itemPrice = 8.00,
        remainQuantity = 5
    ),
    MenuItem(
        menuId = "M0006",
        categoryId = "C0003",
        imageRes = R.drawable.currymee,
        itemName = R.string.curry_mee,
        itemDesc = R.string.curry_and_mee,
        itemPrice = 10.00,
        remainQuantity = 5
    ),
    MenuItem(
        menuId = "M0007",
        categoryId = "C0001",
        imageRes = R.drawable.chickenrice,
        itemName = R.string.chicken_rice,
        itemDesc = R.string.chicken_and_rice,
        itemPrice = 6.00,
        remainQuantity = 5
    ),
    MenuItem(
        menuId = "M0008",
        categoryId = "C0002",
        imageRes = R.drawable.tomyammaggi,
        itemName = R.string.tomyam_maggi,
        itemDesc = R.string.tomyam_flavour_maggi_mee,
        itemPrice = 8.00,
        remainQuantity = 5
    ),
    MenuItem(
        menuId = "M0009",
        categoryId = "C0003",
        imageRes = R.drawable.currymee,
        itemName = R.string.curry_mee,
        itemDesc = R.string.curry_and_mee,
        itemPrice = 10.00,
        remainQuantity = 5
    )
)
