package id.datzon.glasslab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { GlassLabTheme { GlassLabScreen() } }
    }
}

@androidx.compose.runtime.Composable
fun GlassLabTheme(content: @androidx.compose.runtime.Composable () -> Unit) {
    MaterialTheme(
        colorScheme =
            darkColorScheme(
                primary = Accent,
                primaryContainer = Color(0xFF2E4039),
                onPrimaryContainer = Accent,
                secondaryContainer = Color(0xFF2B3934),
                onSecondaryContainer = Accent,
                surfaceContainer = Color(0xFF21272A),
                surfaceContainerHigh = Color(0xFF292F32),
                outline = Color(0xFF48504F),
                outlineVariant = Color(0xFF38403E),
                secondary = Color(0xFF9EBAB3),
                surface = Color(0xFF191D20),
                surfaceVariant = Color(0xFF272D30),
                background = Ink,
                onSurface = Color(0xFFEAF0ED),
                onPrimary = Ink,
            ),
        content = content,
    )
}
