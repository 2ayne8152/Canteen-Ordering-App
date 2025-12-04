// In C:/Androidprojects/Canteen-Ordering-App/app/src/main/java/com/example/canteen/MainActivity.kt

package com.example.canteen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.canteen.ui.theme.CanteenOrderingAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // CanteenOrderingAppTheme is the auto-generated theme from your project.
            CanteenOrderingAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Call the LoginScreen composable to display it.
                    LoginScreen()
                }
            }
        }
    }
}
