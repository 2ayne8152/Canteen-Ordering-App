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
    val categoryId: String
)

val sampleMenuItems = listOf(
    MenuItem("1", "Nasi Lemak", "Coconut rice with sambal", 5.0, 20, "", "food"),
    MenuItem("2", "Chicken Chop", "Grilled chicken with sauce", 10.0, 15, "", "food"),
    MenuItem("3", "Roti Canai", "Flatbread with curry", 2.5, 30, "", "food"),
    MenuItem("4", "Teh Tarik", "Pulled tea", 1.5, 50, "", "beverage"),
    MenuItem("5", "Milo Dinosaur", "Milo with extra powder", 3.0, 40, "", "beverage"),
    MenuItem("6", "Coffee", "Freshly brewed coffee", 2.0, 35, "", "beverage"),
    MenuItem("7", "Cendol", "Shaved ice dessert with coconut milk", 3.5, 20, "", "dessert"),
    MenuItem("8", "Ais Kacang", "Ice dessert with beans and syrup", 3.0, 20, "", "dessert"),
    MenuItem("9", "Kuih Lapis", "Layered colorful cake", 1.0, 25, "", "dessert"),
    MenuItem("10", "Mee Goreng", "Fried noodles with vegetables", 6.0, 15, "", "food"),
    MenuItem("11", "Char Kuey Teow", "Stir-fried flat noodles", 7.0, 10, "", "food"),
    MenuItem("12", "Latte", "Milk coffee", 4.0, 20, "", "beverage"),
    MenuItem("13", "Brownies", "Chocolate brownies", 2.5, 15, "", "dessert"),
    MenuItem("14", "Pasta Carbonara", "Creamy pasta", 12.0, 10, "", "food"),
    MenuItem("15", "Orange Juice", "Fresh orange juice", 3.0, 25, "", "beverage")
)
