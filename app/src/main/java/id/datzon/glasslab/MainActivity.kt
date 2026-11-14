package id.datzon.glasslab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        window.statusBarColor = android.graphics.Color.rgb(119, 106, 80)
        @Suppress("DEPRECATION")
        window.navigationBarColor = android.graphics.Color.rgb(23, 22, 18)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFFD6E8C9),
                    secondary = Color(0xFFD8C7B2),
                    surface = Color(0xFF211F1B),
                    background = Color(0xFF71644B)
                )
            ) {
                GlassLabScreen()
            }
        }
    }
}
