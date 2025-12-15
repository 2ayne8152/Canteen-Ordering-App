package com.example.canteen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.canteen.data.MenuItem
import com.example.canteen.ui.screens.UserHomeScreen
import com.example.canteen.viewmodel.usermenu.CartViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val sampleMenu = listOf(
                        MenuItem("1", "Cheeseburger", "Juicy beef patty with cheese", 8.50, 10, ""),
                        MenuItem("2", "Veggie Wrap", "Healthy veggie wrap with hummus", 6.00, 5, ""),
                        MenuItem("3", "French Fries", "Crispy golden fries", 3.50, 20, "")
                    )

                    UserHomeScreen(
                        menuItems = sampleMenu,
                        onItemClick = {}
                    )
                }
            }
        }

    }
}
