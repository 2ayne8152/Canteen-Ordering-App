package com.example.canteen

import android.graphics.drawable.Icon
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.canteen.data.menuItems
import com.example.canteen.ui.screens.usermenu.UserMenu
import com.example.canteen.ui.theme.CanteenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CanteenTheme {
                UserMenu(
                    menuItems = menuItems,
                    onItemClick = {},
                    numOfItem = 10,
                    totalPrice = 12.00,
                    onDetailClick = {}
                )
            }
        }
    }
}

