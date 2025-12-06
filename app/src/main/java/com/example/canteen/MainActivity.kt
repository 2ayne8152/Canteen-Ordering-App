package com.example.canteen

import android.graphics.drawable.Icon
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.canteen.ui.screens.staffMenu.MenuItemForm
import com.example.canteen.ui.screens.staffMenu.MenuListPage
import com.example.canteen.ui.screens.staffMenu.MenuListPagePreview
import com.example.canteen.ui.theme.CanteenTheme
import com.example.menumanagement.StaffDashboardScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CanteenTheme {
                StaffDashboardScreen()
            }
        }
    }
}

