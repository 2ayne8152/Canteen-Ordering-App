package com.example.canteen

import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.example.canteen.ui.screens.CanteenScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.canteen.ui.screens.CanteenApp
import com.example.canteen.ui.screens.loginscreens.LoginScreen
import com.example.canteen.ui.screens.loginscreens.StaffLoginScreen
import com.example.canteen.ui.screens.payment.MakePayment
import com.example.canteen.ui.screens.staffMenu.MenuItemForm
import com.example.canteen.ui.screens.staffMenu.MenuListPage
import com.example.canteen.ui.screens.staffMenu.MenuListPagePreview
import com.example.canteen.ui.screens.usermenu.UserMenu
import com.example.canteen.ui.theme.CanteenTheme
import com.example.menumanagement.StaffDashboardScreen

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CanteenTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MakePayment()
                }
            }
        }
    }
}

