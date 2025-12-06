package com.example.canteen.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.canteen.R

data class MenuItem(
    @DrawableRes val imageRes: Int,
    @StringRes val itemName: Int,
    @StringRes val itemDesc: Int,
    val itemPrice: Double
)

// For Testing Only, will retrieve from database in future
val menuItems = listOf(
    MenuItem(
        imageRes = R.drawable.chickenrice,
        itemName = R.string.chicken_rice,
        itemDesc = R.string.chicken_and_rice,
        itemPrice = 6.00
    ),
    MenuItem(
        imageRes = R.drawable.tomyammaggi,
        itemName = R.string.tomyam_maggi,
        itemDesc = R.string.tomyam_flavour_maggi_mee,
        itemPrice = 8.00
    ),
    MenuItem(
        imageRes = R.drawable.currymee,
        itemName = R.string.curry_mee,
        itemDesc = R.string.curry_and_mee,
        itemPrice = 10.00
    ),
    MenuItem(
        imageRes = R.drawable.chickenrice,
        itemName = R.string.chicken_rice,
        itemDesc = R.string.chicken_and_rice,
        itemPrice = 6.00
    ),
    MenuItem(
        imageRes = R.drawable.tomyammaggi,
        itemName = R.string.tomyam_maggi,
        itemDesc = R.string.tomyam_flavour_maggi_mee,
        itemPrice = 8.00
    ),
    MenuItem(
        imageRes = R.drawable.currymee,
        itemName = R.string.curry_mee,
        itemDesc = R.string.curry_and_mee,
        itemPrice = 10.00
    ),
    MenuItem(
        imageRes = R.drawable.chickenrice,
        itemName = R.string.chicken_rice,
        itemDesc = R.string.chicken_and_rice,
        itemPrice = 6.00
    ),
    MenuItem(
        imageRes = R.drawable.tomyammaggi,
        itemName = R.string.tomyam_maggi,
        itemDesc = R.string.tomyam_flavour_maggi_mee,
        itemPrice = 8.00
    ),
    MenuItem(
        imageRes = R.drawable.currymee,
        itemName = R.string.curry_mee,
        itemDesc = R.string.curry_and_mee,
        itemPrice = 10.00
    )
)
